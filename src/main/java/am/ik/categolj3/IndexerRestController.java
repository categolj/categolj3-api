package am.ik.categolj3;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/indexer")
public class IndexerRestController {
    @Autowired
    Indexer indexer;

    @RequestMapping(path = "reindex")
    CompletableFuture<Void> reindex() {
        return indexer.reindex();
    }
}
