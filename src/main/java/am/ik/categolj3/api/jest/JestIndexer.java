package am.ik.categolj3.api.jest;

import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.git.GitStore;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        List<Entry> entries = gitStore.loadEntries();
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

    public void bulkUpdate(List<Long> deleteIds, List<Entry> updateEntries) {
        bulkUpdateRecursively(deleteIds, updateEntries, 0);
    }

    private void bulkUpdateRecursively(List<Long> deleteIds, List<Entry> updateEntries, int count) {
        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (Long id : deleteIds) {
            Delete delete = new Delete.Builder(id.toString())
                    .refresh(true)
                    .index(Entry.INDEX_NAME)
                    .type(Entry.DOC_TYPE)
                    .build();
            bulkBuilder.addAction(delete);
        }
        for (Entry entry : updateEntries) {
            Index index = new Index.Builder(entry)
                    .refresh(true)
                    .index(Entry.INDEX_NAME)
                    .type(Entry.DOC_TYPE)
                    .build();
            bulkBuilder.addAction(index);
        }
        try {
            jestClient.execute(bulkBuilder.build());
        } catch (Exception e) {
            log.warn("[" + count + "] bulkUpdate failure delete=" + deleteIds + ", update=" + updateEntries, e);
            if (++count < 5) {
                try {
                    TimeUnit.SECONDS.sleep(count);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                bulkUpdateRecursively(deleteIds, updateEntries, count);
            } else {
                throw new IllegalStateException("failed delete=" + deleteIds + ", update=" + updateEntries, e);
            }
        }
    }
}
