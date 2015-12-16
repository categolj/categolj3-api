package am.ik.categolj3.api.git;

import am.ik.categolj3.api.entry.Author;
import am.ik.categolj3.api.entry.Entry;
import com.google.common.collect.Iterables;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class GitStore {
    @Autowired
    GitProperties gitProperties;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    GitPullTask gitPullTask;

    @Autowired
    ApplicationContext applicationContext;

    Cache entryCache;

    Git git;

    AtomicReference<ObjectId> currentHead = new AtomicReference<>();

    public static final ZoneOffset offset = ZonedDateTime.now().getOffset();

    public Entry get(Long entryId) {
        return this.entryCache.get(entryId, Entry.class);
    }

    public CompletableFuture<PullResult> pull() {
        return this.gitPullTask.pull(this.git).thenApply(x -> {
            this.syncHead();
            return x;
        });
    }

    void syncHead() {
        log.info("Syncing HEAD...");

        ObjectId oldHead = this.currentHead.get();
        ObjectId newHead = this.head();

        try (Repository repository = this.git.getRepository()) {
            DiffFormatter diffFormatter = new DiffFormatter(System.out);
            diffFormatter.setRepository(repository);
            RevWalk walk = new RevWalk(repository);
            try {
                RevCommit fromCommit = walk.parseCommit(oldHead);
                RevCommit toCommit = walk.parseCommit(newHead);
                List<DiffEntry> list = diffFormatter.scan(fromCommit.getTree(),
                        toCommit.getTree());

                AtomicBoolean changed = new AtomicBoolean(false);
                list.forEach(diff -> {
                    log.info("[{}]\tnew={}\told={}", diff.getChangeType(), diff
                            .getNewPath(), diff.getOldPath());
                    if (diff.getOldPath() != null) {
                        Path path = Paths.get(gitProperties.getBaseDir()
                                .getAbsolutePath() + "/" + diff.getOldPath());
                        if (Entry.isPublic(path)) {
                            Long entryId = Entry.parseEntryId(path);
                            log.info("evict Entry({})", entryId);
                            this.entryCache.evict(entryId);
                            changed.set(true);
                        }
                    }
                    if (diff.getNewPath() != null) {
                        Path path = Paths.get(gitProperties.getBaseDir()
                                .getAbsolutePath() + "/" + diff.getNewPath());
                        Entry.loadFromFile(path).ifPresent(entry -> {
                            log.info("put Entry({})", entry.getEntryId());
                            this.entryCache.put(entry.getEntryId(), entry);
                            changed.set(true);
                        });
                    }
                });
                if (changed.get()) {
                    this.applicationContext.publishEvent(new GitEntryChangedEvent(OffsetDateTime.now()));
                } else {
                    log.info("No change");
                }
            } finally {
                walk.dispose();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        this.currentHead.set(newHead);
    }

    public ObjectId head() {
        try (Repository repository = this.git.getRepository()) {
            return repository.resolve("HEAD");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void forceRefreshAll() {
        for (File f : this.getContents()) {
            Entry.loadFromFile(f.toPath()).ifPresent(entry -> {
                entryCache.put(entry.getEntryId(), entry);
            });
        }
    }

    @PostConstruct
    void load() {
        this.entryCache = this.cacheManager.getCache("entry");
        this.git = this.getGitDirectory();
        this.currentHead.set(this.head());
        this.forceRefreshAll();
    }

    @PreDestroy
    void destroy() {
        this.git.close();
    }

    @Component
    @Slf4j
    public static class GitPullTask {
        @Autowired
        GitProperties gitProperties;

        @Async
        public CompletableFuture<PullResult> pull(Git git) {
            log.info("git pull {}", gitProperties.getUri());
            try {
                PullCommand pull = git.pull();
                gitProperties.credentialsProvider().ifPresent(
                        pull::setCredentialsProvider);
                return CompletableFuture.completedFuture(pull.call());
            } catch (GitAPIException e) {
                CompletableFuture<PullResult> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        }
    }

    public List<File> getContents() {
        String contentsDir = gitProperties.getBaseDir().getAbsolutePath() + "/"
                + gitProperties.getContentDir();
        File[] files = new File(contentsDir).listFiles(f -> Entry.isPublic(f
                .toPath()));
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    public Pair<Author, Author> getAuthor(Path path) {
        Path p = gitProperties.getBaseDir().toPath().relativize(path);
        try {
            Iterable<RevCommit> commits = git.log().addPath(p.toString()).call();
            RevCommit created = Iterables.getLast(commits);
            RevCommit updated = Iterables.getFirst(commits, created);
            return new Pair<>(author(created), author(updated));
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    Author author(RevCommit commit) {
        String name = commit != null ? commit.getAuthorIdent().getName() : "";
        Date date = commit != null ? commit.getAuthorIdent().getWhen() : new Date();
        OffsetDateTime o = OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return new Author(name, o);
    }

    Git getGitDirectory() {
        try {
            if (gitProperties.getBaseDir().exists()) {
                if (gitProperties.isInit()) {
                    FileSystemUtils.deleteRecursively(gitProperties
                            .getBaseDir());
                } else {
                    return Git.open(gitProperties.getBaseDir());
                }
            }
            CloneCommand clone = Git.cloneRepository().setURI(gitProperties
                    .getUri()).setDirectory(gitProperties.getBaseDir());
            gitProperties.credentialsProvider().ifPresent(
                    clone::setCredentialsProvider);
            return clone.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }
}
