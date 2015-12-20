package am.ik.categolj3.api.git;

import am.ik.categolj3.api.entry.Author;
import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.jest.JestIndexer;
import am.ik.categolj3.api.jest.JestProperties;
import com.google.common.collect.Iterables;
import lombok.Data;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @Autowired
    JestProperties jestProperties;

    @Autowired
    JestIndexer indexer;

    Cache entryCache;

    Git git;

    AtomicReference<ObjectId> currentHead = new AtomicReference<>();

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

                List<Long> deleted = new ArrayList<>();
                List<Entry> updated = new ArrayList<>();
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
                            deleted.add(entryId);
                        }
                    }
                    if (diff.getNewPath() != null) {
                        Path path = Paths.get(gitProperties.getBaseDir()
                                .getAbsolutePath() + "/" + diff.getNewPath());
                        this.loadEntry(path).ifPresent(entry -> {
                            log.info("put Entry({})", entry.getEntryId());
                            this.entryCache.put(entry.getEntryId(), entry);
                            updated.add(entry);
                        });
                    }
                });
                if (deleted.isEmpty() && updated.isEmpty()) {
                    log.info("No change");
                } else {
                    this.applicationContext.publishEvent(new GitEntryEvents.BulkUpdateEvent(deleted, updated, OffsetDateTime.now()));
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

    public List<Entry> loadEntries() {
        return getContentFiles().stream()
                .map(File::toPath)
                .map(this::loadEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<Entry> loadEntry(Path path) {
        return Entry.loadFromFile(path)
                .map(e -> {
                    Pair<Author, Author> author = getAuthor(path);
                    System.out.println(e.getFrontMatter());
                    if (e.getFrontMatter() != null && e.getFrontMatter().getDate() != null) {
                        // ignore created.date if it is set by frontMatter
                        e.setCreated(new Author(author.getKey().getName(), e.getFrontMatter().getDate()));
                    } else {
                        e.setCreated(author.getKey());
                    }
                    if (e.getFrontMatter() != null && e.getFrontMatter().getUpdated() != null) {
                        // ignore updated.date if it is set by frontMatter
                        e.setUpdated(new Author(author.getValue().getName(), e.getFrontMatter().getUpdated()));
                    } else {
                        e.setUpdated(author.getValue());
                    }
                    return e;
                });
    }

    public void forceRefreshAll() {
        loadEntries().forEach(entry -> {
            entryCache.put(entry.getEntryId(), entry);
        });
    }

    @PostConstruct
    void load() {
        this.entryCache = this.cacheManager.getCache("entry");
        this.git = this.getGitDirectory();
        this.currentHead.set(this.head());
        if (this.gitProperties.isInit()) {
            this.pull().thenAccept(r -> {
                this.forceRefreshAll();
                if (this.jestProperties.isInit()) {
                    this.indexer.reindex();
                }
            });
        } else {
            this.forceRefreshAll();
            if (this.jestProperties.isInit()) {
                this.indexer.reindex();
            }
        }
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

    public List<File> getContentFiles() {
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
            RevCommit updated = Iterables.getFirst(commits, null);
            RevCommit created = Iterables.getLast(commits);
            return new Pair<>(author(created), author(updated == null ? created : updated));
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

    @Data
    public static class Pair<K, V> {
        private final K key;
        private final V value;
    }
}
