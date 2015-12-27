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
package am.ik.categolj3.api.entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontMatter implements Serializable {
    private String title;

    private List<String> tags;

    private List<String> categories;

    @JsonIgnore
    transient private OffsetDateTime date;
    @JsonIgnore
    transient private OffsetDateTime updated;

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
        if (map.containsKey("date")) { // published date
            frontMatter.setDate(OffsetDateTime.ofInstant(((Date) map.get("date")).toInstant(), ZoneId.systemDefault()));
        }
        if (map.containsKey("updated")) { // Updated date
            frontMatter.setUpdated(OffsetDateTime.ofInstant(((Date) map.get("updated")).toInstant(), ZoneId.systemDefault()));
        }
        return frontMatter;
    }
}
