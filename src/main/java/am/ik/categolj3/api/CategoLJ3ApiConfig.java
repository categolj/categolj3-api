package am.ik.categolj3.api;

import am.ik.categolj3.api.category.CategoryService;
import am.ik.categolj3.api.category.InMemoryCategoryService;
import am.ik.categolj3.api.jest.JestProperties;
import am.ik.categolj3.api.tag.InMemoryTagService;
import am.ik.categolj3.api.tag.TagService;
import com.google.gson.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@ComponentScan
@EnableCaching
@EnableAsync
@EnableScheduling
public class CategoLJ3ApiConfig {

    @Bean
    @ConditionalOnMissingBean
    JestClient jestClient(JestProperties jestProperties) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(jestProperties.getConnectionUrl())
                .multiThreaded(true)
                .connTimeout(jestProperties.getConnectionTimeout())
                .readTimeout(jestProperties.getReadTimeout())
                .gson(gson())
                .build());
        return factory.getObject();
    }

    @Bean
    Gson gson() {
        GsonBuilder builder = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                .registerTypeAdapter(OffsetDateTime.class,
                        (JsonDeserializer<OffsetDateTime>) (json, type, context)
                                -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getAsString(), OffsetDateTime::from))
                .registerTypeAdapter(OffsetDateTime.class,
                        (JsonSerializer<OffsetDateTime>) (json, type, context)
                                -> new JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(json)));
        return builder.create();
    }

    @Bean
    @ConditionalOnMissingBean
    TagService tagService(CacheManager cacheManager) {
        return new InMemoryTagService(cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    CategoryService categoryService(CacheManager cacheManager) {
        return new InMemoryCategoryService(cacheManager);
    }
}
