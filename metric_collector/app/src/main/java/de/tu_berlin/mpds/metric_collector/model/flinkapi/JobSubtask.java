package de.tu_berlin.mpds.metric_collector.model.flinkapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobSubtask {

    private String subtaskId;
    private String status;
    private JobVertexMetric metrics;


    public JobSubtask(@JsonProperty("subtask") String id, @JsonProperty("status") String status,@JsonProperty("metrics") JobVertexMetric metrics
                      ) {
        this.status = status;
        this.subtaskId= id;
        this.metrics = metrics;
    }
}
