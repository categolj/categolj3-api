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
package am.ik.categolj3.api.entry;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchEntryOperations {
    Page<Entry> findAll(Pageable pageable, SearchOptions options);

    default Page<Entry> findAll(Pageable pageable) {
        return findAll(pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByTag(String tag, Pageable pageable, SearchOptions options);

    default Page<Entry> findByTag(String tag, Pageable pageable) {
        return findByTag(tag, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByCategories(List<String> categories, Pageable pageable, SearchOptions options);

    default Page<Entry> findByCategories(List<String> categories, Pageable pageable) {
        return findByCategories(categories, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByCreatedBy(String user, Pageable pageable, SearchOptions options);

    default Page<Entry> findByCreatedBy(String user, Pageable pageable) {
        return findByCreatedBy(user, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByUpdatedBy(String user, Pageable pageable, SearchOptions options);

    default Page<Entry> findByUpdatedBy(String user, Pageable pageable) {
        return findByUpdatedBy(user, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByQuery(String q, Pageable pageable, SearchOptions options);

    default Page<Entry> findByQuery(String q, Pageable pageable) {
        return findByQuery(q, pageable, SearchOptions.DEFAULT);
    }

    @Data
    @Builder
    class SearchOptions {
        private boolean excludeContent;

        public static final SearchOptions DEFAULT = SearchOptions.builder()
                .excludeContent(false)
                .build();
    }
}
