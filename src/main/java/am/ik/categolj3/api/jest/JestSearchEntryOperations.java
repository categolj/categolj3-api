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
import am.ik.categolj3.api.entry.SearchEntryOperations;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchQueryBuilder;
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
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JestSearchEntryOperations implements SearchEntryOperations {
    @Autowired
    JestClient jestClient;

    final String[] fieldsExcludeContent = new String[]{
            "entryId",
            "created.name",
            "created.date",
            "updated.name",
            "updated.date",
            "frontMatter.title",
            "frontMatter.tags",
            "frontMatter.categories"};

    @Override
    public Page<Entry> findAll(Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.matchAllQuery();
        return search(query, pageable, options);
    }

    @Override
    public Page<Entry> findByTag(String tag, Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.matchQuery("frontMatter.tags", tag)
                .operator(MatchQueryBuilder.Operator.AND);
        return search(query, pageable, options);
    }

    @Override
    public Page<Entry> findByCategories(List<String> categories, Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.matchQuery("frontMatter.categories", categories)
                .operator(MatchQueryBuilder.Operator.AND);
        return search(query, pageable, options);
    }

    @Override
    public Page<Entry> findByCreatedBy(String user, Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.matchQuery("created.name", user)
                .operator(MatchQueryBuilder.Operator.AND);
        return search(query, pageable, options);
    }

    @Override
    public Page<Entry> findByUpdatedBy(String user, Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.matchQuery("updated.name", user)
                .operator(MatchQueryBuilder.Operator.AND);
        return search(query, pageable, options);
    }

    @Override
    public Page<Entry> findByQuery(String q, Pageable pageable, SearchOptions options) {
        QueryBuilder query = QueryBuilders.simpleQueryStringQuery(q);
        return search(query, pageable, options);
    }

    Page<Entry> search(QueryBuilder query, Pageable pageable, SearchOptions options) {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .query(query)
                    .sort("updated.date", SortOrder.DESC)
                    .sort("entryId", SortOrder.DESC)
                    .from(pageable.getOffset())
                    .size(pageable.getPageSize());
            if (options.isExcludeContent()) {
                sourceBuilder = sourceBuilder.fetchSource(fieldsExcludeContent, new String[]{"content"});
            }
            long count = jestClient.execute(
                    new Count.Builder()
                            .query(new SearchSourceBuilder()
                                    .query(query).toString())
                            .addIndex(Entry.INDEX_NAME)
                            .addType(Entry.DOC_TYPE)
                            .build())
                    .getCount().longValue();
            List<Entry> content = null;
            if (count > 0) {
                content = ((JestResult) jestClient.execute(
                        new Search.Builder(sourceBuilder.toString())
                                .addIndex(Entry.INDEX_NAME)
                                .addType(Entry.DOC_TYPE)
                                .build()))
                        .getSourceAsObjectList(Entry.class);
            } else {
                content = Collections.emptyList();
            }
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
