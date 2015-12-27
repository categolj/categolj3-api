package am.ik.categolj3.api.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Slf4j
public class EventManager {
    final ApplicationEventPublisher publisher;
    final Queue<EntryEvictEvent> entryEvictEventQueue = new ConcurrentLinkedDeque<>();
    final Queue<EntryPutEvent> entryPutEventQueue = new ConcurrentLinkedDeque<>();

    @Autowired
    public EventManager(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 5000)
    public void fireEvents() {
        List<EntryEvictEvent> entryEvictEvents = new ArrayList<>();
        while (!entryEvictEventQueue.isEmpty()) {
            entryEvictEventQueue.add(entryEvictEventQueue.poll());
        }
        if (!entryEvictEvents.isEmpty()) {
            publisher.publishEvent(new EntryEvictEvent.Bulk(entryEvictEvents));
        }

        List<EntryPutEvent> entryPutEvents = new ArrayList<>();
        while (!entryPutEventQueue.isEmpty()) {
            entryPutEvents.add(entryPutEventQueue.poll());
        }
        if (!entryPutEvents.isEmpty()) {
            publisher.publishEvent(new EntryPutEvent.Bulk(entryPutEvents));
        }
    }

    public void registerEntryEvictEvent(EntryEvictEvent event) {
        entryEvictEventQueue.add(event);
    }

    public void registerEntryPutEvent(EntryPutEvent event) {
        entryPutEventQueue.add(event);
    }

    public void registerEntryReindexEvent(EntryReIndexEvent event) {
        publisher.publishEvent(event);
    }
}
