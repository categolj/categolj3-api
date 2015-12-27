package am.ik.categolj3.api.event;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public abstract class BulkEvent<T> implements Serializable {
    private final List<T> events;
}
