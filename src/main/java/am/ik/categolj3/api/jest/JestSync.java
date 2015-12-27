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
