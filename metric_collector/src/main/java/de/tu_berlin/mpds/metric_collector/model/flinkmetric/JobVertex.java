package de.tu_berlin.mpds.metric_collector.model.flinkmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    private JobVertexMetric metrics;


    public JobVertex(@JsonProperty("id") String id, @JsonProperty("status") String status,
                     @JsonProperty("name") String name, @JsonProperty("parallelism") Integer parallelism,
                     @JsonProperty("metrics") JobVertexMetric metrics) {
        this.status = status;
        this.id = id;
        this.name = name;
        this.parallelism = parallelism;
        this.metrics = metrics;
    }
}
