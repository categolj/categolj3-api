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
package am.ik.categolj3.api.tag;

import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.event.EntryEvictEvent;
import am.ik.categolj3.api.event.EntryPutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryTagService implements TagService {

    private final ConcurrentMap<Long, List<String>> tags = new ConcurrentHashMap<>();

    @EventListener
    public void handlePutEntry(EntryPutEvent.Bulk event) {
        if (log.isInfoEnabled()) {
            log.info("bulk put ({})", event.getEvents().size());
        }
        tags.putAll(event.getEvents().stream()
                .map(EntryPutEvent::getEntry)
                .filter(e -> !CollectionUtils.isEmpty(e.getFrontMatter().getTags()))
                .collect(Collectors.toMap(Entry::getEntryId, e -> e.getFrontMatter().getTags())));
    }

    @EventListener
    public void handleEvictEntry(EntryEvictEvent.Bulk event) {
        if (log.isInfoEnabled()) {
            log.info("bulk evict ({})", event.getEvents().size());
        }
        event.getEvents().forEach(e -> tags.remove(e.getEntryId()));
    }

    @Override
    public List<String> findAllOrderByNameAsc() {
        return this.tags.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
