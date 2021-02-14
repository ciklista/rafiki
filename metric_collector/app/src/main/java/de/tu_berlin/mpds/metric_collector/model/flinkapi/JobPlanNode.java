package de.tu_berlin.mpds.metric_collector.model.flinkapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class JobPlanNode {
    private String id;
    private int parallelism;

    public JobPlanNode(@JsonProperty("id") String id, @JsonProperty("parallelism") int parallelism) {
        this.id = id;
        this.parallelism = parallelism;
    }
}
