package am.ik.categolj3.api.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author implements Serializable {
    @JsonProperty("By")
    private String name;
    @JsonProperty("At")
    private OffsetDateTime date;
}
