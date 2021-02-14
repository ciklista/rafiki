package de.tu_berlin.mpds.metric_collector.model.flinkapi;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class JobVertexMetric {

    private BigInteger read_bytes;
    private String read_bytes_complete;
    private BigInteger write_bytes;
    private String write_bytes_complete;
    private BigInteger read_records;
    private String read_records_complete;
    private BigInteger write_records;
    private String write_records_complete;


    public JobVertexMetric(@JsonProperty("read-bytes") BigInteger read_bytes,
                           @JsonProperty("read-bytes-complete") String read_bytes_complete,
                           @JsonProperty("write-bytes") BigInteger write_bytes,
                           @JsonProperty("write-bytes-complete") String write_bytes_complete,
                           @JsonProperty("read-records") BigInteger read_records,
                           @JsonProperty("read-records-complete") String read_records_complete,
                           @JsonProperty("write-records") BigInteger write_records,
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
