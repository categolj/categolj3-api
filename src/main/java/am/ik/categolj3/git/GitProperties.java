package am.ik.categolj3.git;

import java.io.File;
import java.util.Optional;

import lombok.Data;
import lombok.ToString;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "git")
@Data
@ToString(exclude = "password")
public class GitProperties {
    private File baseDir;

    private String contentDir;

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
