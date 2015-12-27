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
package am.ik.categolj3.api;

import am.ik.categolj3.api.category.CategoryService;
import am.ik.categolj3.api.category.InMemoryCategoryService;
import am.ik.categolj3.api.jest.JestProperties;
import am.ik.categolj3.api.tag.InMemoryTagService;
import am.ik.categolj3.api.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.gson.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                .registerTypeAdapter(OffsetDateTime.class,
                        (JsonDeserializer<OffsetDateTime>) (json, type, context)
                                -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.getAsString(), OffsetDateTime::from))
                .registerTypeAdapter(OffsetDateTime.class,
                        (JsonSerializer<OffsetDateTime>) (json, type, context)
                                -> new JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(json))).create();
    }

    @Bean
    @ConditionalOnMissingBean
    ObjectMapper objectMapper() {
        return new Jackson2ObjectMapperBuilder()
                .dateFormat(new StdDateFormat())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    TagService tagService() {
        return new InMemoryTagService();
    }

    @Bean
    @ConditionalOnMissingBean
    CategoryService categoryService() {
        return new InMemoryCategoryService();
    }
}
