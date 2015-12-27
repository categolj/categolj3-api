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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.searchbox.annotations.JestId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry implements Serializable {

    public static final String INDEX_NAME = "entries";

    public static final String DOC_TYPE = "entry";

    @JestId
    private Long entryId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;

    private Author created;
    private Author updated;

    private FrontMatter frontMatter;

    public static boolean isPublic(Path file) {
        return file != null && file.toFile().getName().matches("[0-9]+\\.md");
    }

    public static Long parseEntryId(Path file) {
        String entryId = file.toFile().getName().replace(".md", "");
        return Long.valueOf(entryId);
    }

    public static Optional<Entry> loadFromFile(Path file) {
        if (!isPublic(file)) {
            return Optional.empty();
        }
        try (InputStream stream = Files.newInputStream(file)) {

            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            Entry entry = new Entry();
            entry.setEntryId(parseEntryId(file));
            try {
                {
                    final StringBuilder out = new StringBuilder();
                    final String firstLine = reader.readLine();
                    if (FrontMatter.SEPARATOR.equals(firstLine)) {
                        for (String line = reader.readLine(); line != null
                                && !FrontMatter.SEPARATOR.equals(
                                line); line = reader.readLine()) {
                            out.append(line);
                            out.append(System.lineSeparator());
                        }
                        entry.setFrontMatter(FrontMatter.loadFromYamlString(out
                                .toString()));
                    } else {
                        return Optional.empty();
                    }
                }
                {
                    final StringBuilder out = new StringBuilder();
                    for (String line = reader
                            .readLine(); line != null; line = reader
                            .readLine()) {
                        out.append(line);
                        out.append(System.lineSeparator());
                    }
                    entry.setContent(out.toString().trim());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return Optional.of(entry);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
