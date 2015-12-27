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
import am.ik.categolj3.api.entry.FrontMatter;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.guava.GuavaCacheManager;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InMemoryTagServiceTest {

    @Test
    public void testFindAllOrderByNameAsc_chm() throws Exception {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("entry");
        Cache cache = cacheManager.getCache("entry");
        Entry entry1 = Entry.builder()
                .entryId(1L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Java", "Spring", "Spring Boot"))
                        .build())
                .build();
        Entry entry2 = Entry.builder()
                .entryId(2L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Java", "Spring", "Java EE"))
                        .build())
                .build();
        Entry entry3 = Entry.builder()
                .entryId(3L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Cloud Foundry"))
                        .build())
                .build();
        cache.put(entry1.getEntryId(), entry1);
        cache.put(entry2.getEntryId(), entry2);
        cache.put(entry3.getEntryId(), entry3);

        InMemoryTagService tagService = new InMemoryTagService(cacheManager);
        assertThat(tagService.findAllOrderByNameAsc(), is(Arrays.asList("Cloud Foundry", "Java", "Java EE", "Spring", "Spring Boot")));
    }

    @Test
    public void testFindAllOrderByNameAsc_guava() throws Exception {
        GuavaCacheManager cacheManager = new GuavaCacheManager("entry");
        Cache cache = cacheManager.getCache("entry");
        Entry entry1 = Entry.builder()
                .entryId(1L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Java", "Spring", "Spring Boot"))
                        .build())
                .build();
        Entry entry2 = Entry.builder()
                .entryId(2L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Java", "Spring", "Java EE"))
                        .build())
                .build();
        Entry entry3 = Entry.builder()
                .entryId(3L)
                .frontMatter(FrontMatter.builder()
                        .tags(Arrays.asList("Cloud Foundry"))
                        .build())
                .build();
        cache.put(entry1.getEntryId(), entry1);
        cache.put(entry2.getEntryId(), entry2);
        cache.put(entry3.getEntryId(), entry3);

        InMemoryTagService tagService = new InMemoryTagService(cacheManager);
        assertThat(tagService.findAllOrderByNameAsc(), is(Arrays.asList("Cloud Foundry", "Java", "Java EE", "Spring", "Spring Boot")));
    }
}