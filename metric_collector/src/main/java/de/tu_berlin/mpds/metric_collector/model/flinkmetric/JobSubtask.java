package de.tu_berlin.mpds.metric_collector.model.flinkmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobSubtask {

    private String id;
    private String status;
    private JobVertexMetric metrics;


    public JobSubtask(@JsonProperty("id") String id, @JsonProperty("status") String status,
                      @JsonProperty("metrics") JobVertexMetric metrics) {
        this.status = status;
        this.id = id;
        this.metrics = metrics;
    }
}
