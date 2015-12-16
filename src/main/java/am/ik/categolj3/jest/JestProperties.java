package am.ik.categolj3.jest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jest")
@Component
@Data
public class JestProperties {
    private String connectionUrl;
}
