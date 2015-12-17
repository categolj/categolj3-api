package am.ik.categolj3.api.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
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

    @Before
    public void before() throws Exception {
        EntryProperties properties = new EntryProperties();
        this.entryService = mock(EntryService.class);

        EntryRestController entryRestController = new EntryRestController();
        entryRestController.entryProperties = properties;
        entryRestController.entryService = entryService;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setDateFormat(new ISO8601DateFormat());
        objectMapper.registerModule(new JavaTimeModule());
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
                .thenReturn(new PageImpl<>(Arrays.asList(
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
                                .build())));
        this.mockMvc
                .perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberOfElements", is(2)))
                .andExpect(jsonPath("number", is(0)))
                .andDo(document("get-entries"));
    }

    @Test
    public void getEntriesPage1() throws Exception {
        when(this.entryService.findAll(anyObject()))
                .thenReturn(new PageImpl<>(Arrays.asList(
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
                                .build()), new PageRequest(1, 2), 10L));
        this.mockMvc
                .perform(get("/api/entries?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberOfElements", is(2)))
                .andExpect(jsonPath("number", is(1)))
                .andDo(document("get-entries-page1"));
    }
}