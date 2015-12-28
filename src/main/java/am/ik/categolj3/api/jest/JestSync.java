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
package am.ik.categolj3.api.jest;

import am.ik.categolj3.api.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Slf4j
public class JestSync {
    @Autowired
    JestIndexer indexer;
    @Autowired
    JestProperties jestProperties;
    @Autowired
    EventManager eventManager;

    @EventListener
    public void handleBulkDelete(EntryEvictEvent.Bulk e) {
        AppState state = eventManager.getState();
        if (state == AppState.INITIALIZED || jestProperties.isInit()) {
            if (log.isInfoEnabled()) {
                log.info("Bulk delete ({})", e.getEvents().size());
            }
            try {
                indexer.bulkDelete(e.getEvents().stream().map(EntryEvictEvent::getEntryId).collect(Collectors.toList()));
            } catch (Exception ex) {
                log.warn("Failed to bulk delete", ex);
                e.getEvents().forEach(eventManager::registerEntryEvictEvent);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Skip to bulk delete (status={},jest.init={})", state, jestProperties.isInit());
            }
        }
    }

    @EventListener
    public void handleBulkUpdate(EntryPutEvent.Bulk e) {
        AppState state = eventManager.getState();
        if (state == AppState.INITIALIZED || jestProperties.isInit()) {
            if (log.isInfoEnabled()) {
                log.info("Bulk update ({})", e.getEvents().size());
            }
            try {
                indexer.bulkUpdate(e.getEvents().stream().map(EntryPutEvent::getEntry).collect(Collectors.toList()));
            } catch (Exception ex) {
                log.warn("Failed to bulk update", ex);
                e.getEvents().forEach(eventManager::registerEntryPutEvent);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Skip to bulk update (status={},jest.init={})", state, jestProperties.isInit());
            }
        }
    }

    @EventListener
    public void handleReindex(EntryReIndexEvent e) {
        indexer.reindex();
    }
}
