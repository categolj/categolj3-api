package am.ik.categolj3.api.jest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jest")
@Component
@Data
public class JestProperties {
    private String connectionUrl;
    private int connectionTimeout = 10000;
    private int readTimeout = 10000;
}