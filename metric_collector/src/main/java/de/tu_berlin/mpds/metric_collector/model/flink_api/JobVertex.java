package de.tu_berlin.mpds.metric_collector.model.flink_api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobVertex {

    private String id;
    private String name;
    private Integer parallelism;
    private String status;


    public JobVertex(@JsonProperty("id") String id, @JsonProperty("status") String status,
                     @JsonProperty("name") String name, @JsonProperty("parallelism") Integer parallelism) {
        this.status = status;
        this.id = id;
        this.name = name;
        this.parallelism = parallelism;
    }
}
