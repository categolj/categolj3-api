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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntryServiceImpl implements EntryService {
    @Autowired
    SimpleEntryOperations simpleEntryOperations;

    @Autowired
    SearchEntryOperations searchEntryOperations;

    @Override
    public Page<Entry> findAll(Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findAll(pageable, options);
    }

    @Override
    public Page<Entry> findByTag(String tag, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByTag(tag, pageable, options);
    }

    @Override
    public Page<Entry> findByCategories(List<String> categories, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByCategories(categories, pageable, options);
    }

    @Override
    public Page<Entry> findByCreatedBy(String user, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByCreatedBy(user, pageable, options);
    }

    @Override
    public Page<Entry> findByUpdatedBy(String user, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByUpdatedBy(user, pageable, options);
    }

    @Override
    public Page<Entry> findByQuery(String keyword, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByQuery(keyword, pageable, options);
    }

    @Override
    public Entry findOne(Long entryId) {
        return this.simpleEntryOperations.findOne(entryId);
    }
}
