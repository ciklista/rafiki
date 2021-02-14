package de.tu_berlin.mpds.metric_collector.model.flinkapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
public class JobVertex {

    private String id;
    private String name;
    private Integer parallelism;
    private String status;
    private List<JobSubtask> subtasks;


    public JobVertex(@JsonProperty("id") String id, @JsonProperty("status") String status,
                     @JsonProperty("name") String name, @JsonProperty("parallelism") Integer parallelism) {
        this.status = status;
        this.id = id;
        this.name = name;
        this.parallelism = parallelism;
    }
}
