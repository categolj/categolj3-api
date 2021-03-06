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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class EntryRestControllerDocumentation {
    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");

    MockMvc mockMvc;
    EntryService entryService;

    @Autowired
    WebApplicationContext context;

    OffsetDateTime now = OffsetDateTime.now();

    List<Entry> mockEntries = Arrays.asList(
            Entry.builder()
                    .entryId(2L)
                    .content("Spring Boot!")
                    .frontMatter(FrontMatter.builder()
                            .title("Hello Spring Boot")
                            .categories(Arrays.asList("Programming", "Java", "Spring", "Boot"))
                            .tags(Arrays.asList("Java", "Spring", "SpringBoot"))
                            .build())
                    .created(Author.builder().name("making").date(now).build())
                    .updated(Author.builder().name("making").date(now).build())
                    .build(),
            Entry.builder()
                    .entryId(1L)
                    .content("Java8!")
                    .frontMatter(FrontMatter.builder()
                            .title("Hello Java8")
                            .categories(Arrays.asList("Programming", "Java"))
                            .tags(Arrays.asList("Java", "Java8", "Stream"))
                            .build())
                    .created(Author.builder().name("making").date(now).build())
                    .updated(Author.builder().name("making").date(now).build())
                    .build());

    @Before
    public void before() throws Exception {
        EntryProperties properties = new EntryProperties();
        this.entryService = mock(EntryService.class);

        EntryRestController entryRestController = new EntryRestController();
        entryRestController.entryProperties = properties;
        entryRestController.entryService = entryService;

        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .dateFormat(new StdDateFormat())
                .indentOutput(true)
                .build();
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(entryRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(jackson2HttpMessageConverter)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void getEntries() throws Exception {
        when(this.entryService.findAll(anyObject()))
                .thenReturn(new PageImpl<>(mockEntries));
        this.mockMvc
                .perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[0].entryId", is(2)))
                .andExpect(jsonPath("$.content[1].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[1].entryId", is(1)))
                .andDo(document("get-entries"));
    }

    @Test
    public void getEntriesExcludeContent() throws Exception {
        when(this.entryService.findAll(anyObject(), eq(SearchEntryOperations.SearchOptions.builder().excludeContent(true).build())))
                .thenReturn(new PageImpl<>(mockEntries.stream().peek(e -> e.setContent(null)).collect(Collectors.toList())));
        this.mockMvc
                .perform(get("/api/entries").param("excludeContent", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.content[0].entryId", is(2)))
                .andExpect(jsonPath("$.content[1].content").doesNotExist())
                .andExpect(jsonPath("$.content[1].entryId", is(1)))
                .andDo(document("get-entries-exclude-content"));
    }

    @Test
    public void getEntriesPage1() throws Exception {
        when(this.entryService.findAll(anyObject()))
                .thenReturn(new PageImpl<>(mockEntries, new PageRequest(1, 2), 10L));
        this.mockMvc
                .perform(get("/api/entries")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.totalElements", is(10)))
                .andExpect(jsonPath("$.content[0].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[0].entryId", is(2)))
                .andExpect(jsonPath("$.content[1].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[1].entryId", is(1)))
                .andDo(document("get-entries-page1"));
    }

    @Test
    public void getEntriesByTag() throws Exception {
        when(this.entryService.findByTag(eq("Java"), anyObject()))
                .thenReturn(new PageImpl<>(mockEntries));
        this.mockMvc
                .perform(get("/api/tags/Java/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[0].entryId", is(2)))
                .andExpect(jsonPath("$.content[1].content", is(notNullValue())))
                .andExpect(jsonPath("$.content[1].entryId", is(1)))
                .andDo(document("get-entries-by-tag"));
    }
}