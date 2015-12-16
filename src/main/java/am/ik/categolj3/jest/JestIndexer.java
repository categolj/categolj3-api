package am.ik.categolj3.jest;

import am.ik.categolj3.entry.Author;
import am.ik.categolj3.entry.Entry;
import am.ik.categolj3.git.GitStore;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JestIndexer {
    @Autowired
    GitStore gitStore;

    @Autowired
    JestClient jestClient;

    @Async
    public CompletableFuture<Void> reindex() {
        log.info("re-indexing ...");
        List<Entry> entries = gitStore.getContents().stream()
                .map(f -> Entry.loadFromFile(f.toPath()).map(e -> {
                    Pair<Author, Author> author = gitStore.getAuthor(f.toPath());
                    e.setCreated(author.getKey());
                    e.setUpdated(author.getValue());
                    return e;
                }))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (Entry entry : entries) {
            Index index = new Index.Builder(entry)
                    .refresh(true)
                    .index(Entry.INDEX_NAME)
                    .type(Entry.DOC_TYPE)
                    .build();
            bulkBuilder.addAction(index);
        }
        try {
            jestClient.execute(bulkBuilder.build());
            log.info("re-indexed!");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
    }
}
