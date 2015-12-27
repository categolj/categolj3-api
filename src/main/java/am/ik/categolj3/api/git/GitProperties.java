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
package am.ik.categolj3.api.git;

import java.io.File;
import java.util.Optional;

import lombok.Data;
import lombok.ToString;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "git")
@Data
@ToString(exclude = "password")
public class GitProperties {
    @NotNull
    private File baseDir;
    @NotEmpty
    private String contentDir;
    @NotEmpty
    private String uri;

    private boolean init = false;

    private String username;

    private char[] password;

    public Optional<UsernamePasswordCredentialsProvider> credentialsProvider() {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return Optional.of(
                new UsernamePasswordCredentialsProvider(username, password));
    }
}
