package am.ik.categolj3.api.entry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "entry")
@Data
public class EntryProperties {
    private String categoriesSeparator;
}
