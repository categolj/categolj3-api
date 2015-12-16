package am.ik.categolj3;

import com.google.gson.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
                .multiThreaded(true)
                .connTimeout(10000)
                .readTimeout(10000)
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
}
