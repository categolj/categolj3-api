package com.example;

import am.ik.categolj3.api.EnableCategoLJ3ApiServer;
import am.ik.categolj3.api.jest.JestProperties;
import am.ik.marked4j.Marked;
import am.ik.marked4j.MarkedBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableCategoLJ3ApiServer
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Profile("cloud")
    @Bean
    JestClient jestClient(JestProperties jestProperties, Gson gson) throws Exception {
        // Using jackson to parse VCAP_SERVICES
        Map result = new ObjectMapper().readValue(System.getenv("VCAP_SERVICES"), HashMap.class);

        String connectionUrl = (String) ((Map) ((Map) ((List)
                result.get("searchly")).get(0)).get("credentials")).get("uri");
        // Configuration
        HttpClientConfig clientConfig = new HttpClientConfig.Builder(connectionUrl)
                .multiThreaded(true)
                .connTimeout(jestProperties.getConnectionTimeout())
                .readTimeout(jestProperties.getReadTimeout())
                .gson(gson)
                .build();

        // Construct a new Jest client according to configuration via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);
        return factory.getObject();
    }

    @Bean
    Marked marked() {
        return new MarkedBuilder()
                .breaks(true)
                .build();
    }
}
