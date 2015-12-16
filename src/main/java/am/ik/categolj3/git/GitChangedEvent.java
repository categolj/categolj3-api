package am.ik.categolj3.git;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class GitChangedEvent implements Serializable {
    private final OffsetDateTime time;
}
