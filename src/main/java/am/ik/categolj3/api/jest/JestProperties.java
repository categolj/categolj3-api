package am.ik.categolj3.api.jest;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jest")
@Component
@Data
public class JestProperties {
    @NotEmpty
    @URL
    private String connectionUrl;
    private int connectionTimeout = 10000;
    private int readTimeout = 10000;
}
