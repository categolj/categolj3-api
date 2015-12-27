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
package am.ik.categolj3.api.git;

import am.ik.categolj3.api.entry.Entry;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public class GitEntryEvents {
    @Data
    public static class BulkUpdateEvent implements Serializable {
        private final List<Long> deleteIds;
        private final List<Entry> updateEntries;
        private final OffsetDateTime time;
    }

    @Data
    public static class RefreshEvent implements Serializable {
        private final OffsetDateTime time;
    }
}
