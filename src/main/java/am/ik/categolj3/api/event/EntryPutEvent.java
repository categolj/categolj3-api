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

import am.ik.categolj3.api.entry.Entry;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EntryPutEvent implements Serializable {
    private final Long entryId;
    private final Entry entry;

    public EntryPutEvent(Entry entry) {
        this.entryId = entry.getEntryId();
        this.entry = entry;
    }

    public static class Bulk extends BulkEvent<EntryPutEvent> {

        public Bulk(List<EntryPutEvent> events) {
            super(events);
        }
    }
}
