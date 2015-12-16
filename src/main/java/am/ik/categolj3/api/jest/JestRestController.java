package am.ik.categolj3.api.jest;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/jest")
public class JestRestController {
    @Autowired
    JestIndexer indexer;

    @RequestMapping(path = "reindex")
    CompletableFuture<Void> reindex() {
        return indexer.reindex();
    }
}
