package am.ik.categolj3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class CategoLJ3 {

    public static void main(String[] args) {
        SpringApplication.run(CategoLJ3.class, args);
    }

    @Bean
    JestClient jestClient(
            @Value("${jest.connection-url}") String connectionUrl) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(connectionUrl)
                .multiThreaded(true).build());
        return factory.getObject();
    }

}
