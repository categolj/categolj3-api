package am.ik.categolj3.entry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "entry")
@Data
public class EntryProperties {
    private String categoriesSeparator;
}
