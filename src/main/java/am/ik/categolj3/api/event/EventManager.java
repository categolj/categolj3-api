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
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class EventManager {
    final ApplicationEventPublisher publisher;
    final Queue<EntryEvictEvent> entryEvictEventQueue = new ConcurrentLinkedDeque<>();
    final Queue<EntryPutEvent> entryPutEventQueue = new ConcurrentLinkedDeque<>();
    final Queue<AppState> appStateQueue = new ConcurrentLinkedDeque<>();

    private final AtomicReference<AppState> state = new AtomicReference<>(AppState.INITIALIZING);

    @Autowired
    public EventManager(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 5000)
    public void fireEvents() {
        List<EntryEvictEvent> entryEvictEvents = new ArrayList<>();
        while (!entryEvictEventQueue.isEmpty()) {
            entryEvictEvents.add(entryEvictEventQueue.poll());
        }
        if (!entryEvictEvents.isEmpty()) {
            log.info("publish bulk evict event");
            publisher.publishEvent(new EntryEvictEvent.Bulk(entryEvictEvents));
        }

        List<EntryPutEvent> entryPutEvents = new ArrayList<>();
        while (!entryPutEventQueue.isEmpty()) {
            entryPutEvents.add(entryPutEventQueue.poll());
        }
        if (!entryPutEvents.isEmpty()) {
            log.info("publish bulk put event");
            publisher.publishEvent(new EntryPutEvent.Bulk(entryPutEvents));
        }
        while (!appStateQueue.isEmpty()) {
            AppState state = appStateQueue.poll();
            if (state == AppState.INITIALIZED) {
                log.info("Initialized");
            }
            this.state.set(state);
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

    public void setState(AppState state) {
        appStateQueue.add(state);
    }

    public AppState getState() {
        return this.state.get();
    }
}
