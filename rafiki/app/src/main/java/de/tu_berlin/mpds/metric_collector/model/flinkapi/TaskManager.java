package de.tu_berlin.mpds.metric_collector.model.flinkapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class TaskManager {

    private String id;
    private int slotsNumber;

    public TaskManager(@JsonProperty("id") String id, @JsonProperty("slotsNumber") int slotsNumber) {
        this.id = id;
        this.slotsNumber = slotsNumber;
    }

}
