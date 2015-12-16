package am.ik.categolj3.api.jest;

import am.ik.categolj3.api.git.GitEntryEvents;
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
    public void sync(GitEntryEvents.RefreshEvent e) {
        log.info("Syncing Jest ... {}", e);
        indexer.reindex();
    }

    @EventListener
    public void bulkUpdate(GitEntryEvents.BulkUpdateEvent e) {
        log.info("Bulk update ... {}", e);
        indexer.bulkUpdate(e.getDeleteIds(), e.getUpdateEntries());
    }
}
