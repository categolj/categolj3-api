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
package am.ik.categolj3.api.category;

import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.event.EntryEvictEvent;
import am.ik.categolj3.api.event.EntryPutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryCategoryService implements CategoryService {

    private final ConcurrentMap<Long, List<String>> categories = new ConcurrentHashMap<>();

    @EventListener
    public void handlePutEntry(EntryPutEvent.Bulk event) {
        if (log.isInfoEnabled()) {
            log.info("bulk put ({})", event.getEvents().size());
        }
        categories.putAll(event.getEvents().stream()
                .map(EntryPutEvent::getEntry)
                .filter(e -> !CollectionUtils.isEmpty(e.getFrontMatter().getCategories()))
                .collect(Collectors.toMap(Entry::getEntryId, e -> e.getFrontMatter().getCategories())));
    }

    @EventListener
    public void handleEvictEntry(EntryEvictEvent.Bulk event) {
        if (log.isInfoEnabled()) {
            log.info("bulk evict ({})", event.getEvents().size());
        }
        event.getEvents().forEach(e -> categories.remove(e.getEntryId()));
    }


    @Override
    public List<List<String>> findAllOrderByNameAsc() {
        return this.categories.values().stream()
                .distinct()
                .sorted(Comparator.comparing(categories -> String.join(",", categories)))
                .collect(Collectors.toList());
    }
}
