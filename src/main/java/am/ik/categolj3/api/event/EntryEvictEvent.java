package am.ik.categolj3.api.event;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EntryEvictEvent implements Serializable {
    private final Long entryId;

    public static class Bulk extends BulkEvent<EntryEvictEvent> {

        public Bulk(List<EntryEvictEvent> events) {
            super(events);
        }
    }
}
