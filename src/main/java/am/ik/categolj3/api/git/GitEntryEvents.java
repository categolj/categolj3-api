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
