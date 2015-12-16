package am.ik.categolj3.jest;

import am.ik.categolj3.git.GitEntryChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JestSync {
    @Autowired
    JestIndexer indexer;

    @EventListener
    public void sync(GitEntryChangedEvent e) {
        log.info("Syncing Jest ...");
        indexer.reindex();
    }
}
