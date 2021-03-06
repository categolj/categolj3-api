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

    public void bulkDelete(List<Long> deleteIds) throws Exception {
        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (Long id : deleteIds) {
            Delete delete = new Delete.Builder(id.toString())
                    .refresh(true)
                    .index(Entry.INDEX_NAME)
                    .type(Entry.DOC_TYPE)
                    .build();
            bulkBuilder.addAction(delete);
        }
        jestClient.execute(bulkBuilder.build());
    }

    public void bulkUpdate(List<Entry> updateEntries) throws Exception {
        Bulk.Builder bulkBuilder = new Bulk.Builder();
        for (Entry entry : updateEntries) {
            Index index = new Index.Builder(entry)
                    .refresh(true)
                    .index(Entry.INDEX_NAME)
                    .type(Entry.DOC_TYPE)
                    .build();
            bulkBuilder.addAction(index);
        }
        jestClient.execute(bulkBuilder.build());
    }

}
