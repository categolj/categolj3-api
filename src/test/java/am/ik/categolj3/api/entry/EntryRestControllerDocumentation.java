package am.ik.categolj3.api.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EntryRestControllerDocumentation {
    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void before() throws Exception {
        EntryProperties properties = new EntryProperties();
        properties.setCategoriesSeparator(",");
        EntryService entryService = mock(EntryService.class);
        when(entryService.findAll((Pageable) anyObject())).thenReturn(new PageImpl<Entry>(Arrays.asList(new Entry(), new Entry())));

        EntryRestController entryRestController = new EntryRestController();
        entryRestController.entryProperties = properties;
        entryRestController.entryService = entryService;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(entryRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(jackson2HttpMessageConverter)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void getEntries() throws Exception {
        this.mockMvc
                .perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andDo(document("get-entries"));
    }
}