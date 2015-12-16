package am.ik.categolj3.api.jest;

import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.entry.SearchEntryOperations;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class JestSearchEntryOperations implements SearchEntryOperations {
    @Autowired
    JestClient jestClient;

    @Override
    public Page<Entry> findAll(Pageable pageable) {
        QueryBuilder query = QueryBuilders.matchAllQuery();
        return search(query, pageable);
    }

    @Override
    public Page<Entry> findByTag(String tag, Pageable pageable) {
        QueryBuilder query = QueryBuilders.termQuery("frontMatter.tags", tag);
        return search(query, pageable);
    }

    @Override
    public Page<Entry> findByCategories(List<String> categories, Pageable pageable) {
        QueryBuilder query = QueryBuilders.termsQuery("frontMatter.categories", categories)
                .minimumMatch(categories.size());
        return search(query, pageable);
    }

    @Override
    public Page<Entry> findByCreatedBy(String user, Pageable pageable) {
        QueryBuilder query = QueryBuilders.termQuery("updated.name", user);
        return search(query, pageable);
    }

    @Override
    public Page<Entry> findByQuery(String q, Pageable pageable) {
        QueryBuilder query = QueryBuilders.simpleQueryStringQuery(q);
        return search(query, pageable);
    }

    Page<Entry> search(QueryBuilder query, Pageable pageable) {
        try {
            List<Entry> content = ((JestResult) jestClient.execute(
                    new Search.Builder(
                            new SearchSourceBuilder()
                                    .query(query)
                                    .sort("updated.date", SortOrder.DESC)
                                    .sort("entryId", SortOrder.DESC)
                                    .from(pageable.getOffset())
                                    .size(pageable.getPageSize())
                                    .toString())
                            .addIndex(Entry.INDEX_NAME)
                            .addType(Entry.DOC_TYPE)
                            .build()))
                    .getSourceAsObjectList(Entry.class);
            long count = jestClient.execute(
                    new Count.Builder()
                            .query(new SearchSourceBuilder()
                                    .query(query).toString())
                            .addIndex(Entry.INDEX_NAME)
                            .addType(Entry.DOC_TYPE)
                            .build())
                    .getCount().longValue();
            return new PageImpl<>(content, pageable, count);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @PostConstruct
    void init() throws Exception {
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
