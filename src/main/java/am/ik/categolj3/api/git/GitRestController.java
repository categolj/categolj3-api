package am.ik.categolj3.api.git;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/git")
public class GitRestController {
    @Autowired
    GitStore gitStore;

    @RequestMapping(path = "pull", method = RequestMethod.GET)
    CompletableFuture<String> pull() {
        return gitStore.pull().thenApply(r -> {
            return r.toString();
        }).exceptionally(Throwable::toString);
    }
}
