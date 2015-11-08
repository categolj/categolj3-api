package am.ik.categolj3.jest;

import java.util.List;

import javax.annotation.PostConstruct;

import am.ik.categolj3.entry.Entry;
import am.ik.categolj3.entry.SearchEntryOperations;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;

@Component
@Slf4j
public class JestSearchEntryOperations implements SearchEntryOperations {
    @Autowired
    JestClient jestClient;

    @Override
    public Page<Entry> findAll(Pageable pageable) {
        try {
            List<Entry> content = ((JestResult) jestClient.execute(
                    new Search.Builder(new SearchSourceBuilder().from(pageable
                            .getOffset()).size(pageable.getPageSize())
                            .toString()).addIndex(Entry.INDEX_NAME).addType(
                                    Entry.DOC_TYPE).build()))
                                            .getSourceAsObjectList(Entry.class);
            long count = jestClient.execute(new Count.Builder().addIndex(
                    Entry.INDEX_NAME).addType(Entry.DOC_TYPE).build())
                    .getCount().longValue();
            return new PageImpl<>(content, pageable, count);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @PostConstruct
    void init() throws Exception {
        {
            IndicesExists indicesExists = new IndicesExists.Builder(Entry.INDEX_NAME)
                    .build();
            JestResult result = jestClient.execute(indicesExists);

            if (!result.isSucceeded()) {
                log.info("Create index {} ...", Entry.INDEX_NAME);
                // Create articles index
                CreateIndex createIndex = new CreateIndex.Builder(Entry.INDEX_NAME)
                        .build();
                String json = jestClient.execute(createIndex).getJsonString();
                log.info("Created index {}", json);
            }
        }
    }
}
