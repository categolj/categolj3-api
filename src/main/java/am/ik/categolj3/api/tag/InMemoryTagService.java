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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryTagService implements TagService {
    final Cache entryCache;

    public InMemoryTagService(CacheManager cacheManager) {
        this.entryCache = cacheManager.getCache("entry");
        if (this.entryCache == null) {
            throw new IllegalArgumentException("cache named 'entry' is not found!");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> findAllOrderByNameAsc() {
        Object nativeCache = entryCache.getNativeCache();
        Map<Object, Entry> cache;
        if (nativeCache instanceof com.google.common.cache.Cache) {
            cache = ((com.google.common.cache.Cache) nativeCache).asMap();
        } else if (nativeCache instanceof Map) {
            cache = (Map<Object, Entry>) entryCache.getNativeCache();
        } else {
            log.warn("native cache is not map -> {}", nativeCache);
            return Collections.emptyList();
        }
        return cache.values().stream()
                .flatMap(e -> e.getFrontMatter().getTags().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
