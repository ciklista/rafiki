package de.tu_berlin.mpds.metric_collector.model.flinkmetric;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobVertexMetric {

    private Integer read_bytes;
    private String read_bytes_complete;
    private Integer write_bytes;
    private String write_bytes_complete;
    private Integer read_records;
    private String read_records_complete;
    private Integer write_records;
    private String write_records_complete;


    public JobVertexMetric(@JsonProperty("read-bytes") Integer read_bytes,
                           @JsonProperty("read-bytes-complete") String read_bytes_complete,
                           @JsonProperty("write-bytes") Integer write_bytes,
                           @JsonProperty("write-bytes-complete") String write_bytes_complete,
                           @JsonProperty("read-records") Integer read_records,
                           @JsonProperty("read-records-complete") String read_records_complete,
                           @JsonProperty("write-records") Integer write_records,
                           @JsonProperty("write-records-complete") String write_records_complete) {
        this.read_bytes = read_bytes;
        this.read_bytes_complete = read_bytes_complete;
        this.write_bytes = write_bytes;
        this.write_bytes_complete = write_bytes_complete;
        this.read_records = read_records;
        this.read_records_complete = read_records_complete;
        this.write_records = write_records;
        this.write_records_complete = write_records_complete;
    }
}
