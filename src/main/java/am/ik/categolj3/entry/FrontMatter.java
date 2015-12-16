package am.ik.categolj3.entry;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class FrontMatter implements Serializable {
    private String title;

    private List<String> tags;

    private List<String> categories;

    @Getter(AccessLevel.NONE)
    private static final Yaml yaml = new Yaml();

    static final String SEPARATOR = "---";

    @SuppressWarnings({"unchecked"})
    public static FrontMatter loadFromYamlString(String string) {
        Map<String, Object> map = (Map<String, Object>) yaml.load(string);
        FrontMatter frontMatter = new FrontMatter();
        frontMatter.setTitle((String) map.getOrDefault("title", "no title"));
        frontMatter.setTags((List<String>) map.computeIfAbsent("tags",
                key -> Collections.emptyList()));
        frontMatter.setCategories((List<String>) map.computeIfAbsent(
                "categories", key -> Collections.emptyList()));
        return frontMatter;
    }
}
