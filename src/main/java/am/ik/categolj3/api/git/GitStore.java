/*
 * Copyright (C) 2015 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.categolj3.api.git;

import am.ik.categolj3.api.entry.Author;
import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.entry.EntryEventFiringCache;
import am.ik.categolj3.api.event.EntryReIndexEvent;
import am.ik.categolj3.api.event.EventManager;
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
    EventManager eventManager;

    @Autowired
    ForceRefreshTask forceRefreshTask;

    @Autowired
    GitPullTask gitPullTask;

    @Autowired
    ApplicationContext applicationContext;

    Cache entryCache;

    Git git;

    AtomicReference<ObjectId> currentHead = new AtomicReference<>();

    public Entry get(Long entryId) {
        Entry entry = this.entryCache.get(entryId, Entry.class);
        if (entry == null) {
            entry = getContentFiles().stream()
                    .filter(f -> String.format("%05d.md", entryId).equals(f.getName()))
                    .map(File::toPath)
                    .map(Entry::loadFromFile)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("The requested entry is not found [" + entryId + "]"));
            this.entryCache.put(entryId, entry);
        }
        return entry;
    }

    public CompletableFuture<PullResult> pull() {
        return this.gitPullTask.pull(this.git).thenApply(x -> {
            this.syncHead();
            return x;
        });
    }

    public List<Entry> loadEntries() {
        return getContentFiles().stream()
                .map(File::toPath)
                .map(this::loadEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void forceRefreshAll() {
        loadEntries().forEach(entry -> {
            entryCache.put(entry.getEntryId(), entry);
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
                        }
                    }
                    if (diff.getNewPath() != null) {
                        Path path = Paths.get(gitProperties.getBaseDir()
                                .getAbsolutePath() + "/" + diff.getNewPath());
                        this.loadEntry(path).ifPresent(entry -> {
                            log.info("put Entry({})", entry.getEntryId());
                            this.entryCache.put(entry.getEntryId(), entry);
                        });
                    }
                });
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

    ObjectId head() {
        try (Repository repository = this.git.getRepository()) {
            return repository.resolve("HEAD");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    Optional<Entry> loadEntry(Path path) {
        return Entry.loadFromFile(path)
                .map(e -> {
                    Pair<Author, Author> author = getAuthor(path);
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


    List<File> getContentFiles() {
        String contentsDir = gitProperties.getBaseDir().getAbsolutePath() + "/"
                + gitProperties.getContentDir();
        File[] files = new File(contentsDir).listFiles(f -> Entry.isPublic(f
                .toPath()));
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    Pair<Author, Author> getAuthor(Path path) {
        Path p = gitProperties.getBaseDir().toPath().relativize(path);
        try {
            Iterable<RevCommit> commits = git.log().addPath(p.toString().replace("\\", "/")).call();
            RevCommit updated = Iterables.getFirst(commits, null);
            RevCommit created = Iterables.getLast(commits, updated);
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

    @PostConstruct
    void load() {
        this.entryCache = new EntryEventFiringCache(this.cacheManager.getCache("entry"), this.eventManager);
        this.git = this.getGitDirectory();
        this.currentHead.set(this.head());
        if (this.gitProperties.isInit()) {
            this.pull().thenAccept(r -> {
                this.forceRefreshTask.forceRefresh(this)
                        .thenAcceptAsync(v -> {
                            this.eventManager.registerEntryReindexEvent(new EntryReIndexEvent(true));
                        });
            }).exceptionally(e -> {
                log.error("error!", e);
                return null;
            });
        } else {
            this.forceRefreshTask.forceRefresh(this)
                    .thenAcceptAsync(v -> {
                        this.eventManager.registerEntryReindexEvent(new EntryReIndexEvent(true));
                    });
        }
    }

    @PreDestroy
    void destroy() {
        this.git.close();
    }

    @Component
    @Slf4j
    public static class ForceRefreshTask {
        @Async
        public CompletableFuture<Void> forceRefresh(GitStore gitStore) {
            try {
                gitStore.forceRefreshAll();
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                CompletableFuture<Void> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        }
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

    @Data
    public static class Pair<K, V> {
        private final K key;
        private final V value;
    }
}
