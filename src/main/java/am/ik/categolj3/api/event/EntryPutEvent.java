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
