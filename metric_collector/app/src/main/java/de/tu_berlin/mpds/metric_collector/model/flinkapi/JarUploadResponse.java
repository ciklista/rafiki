package de.tu_berlin.mpds.metric_collector.model.flinkapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class JarUploadResponse {

    private String filename;
    private String status;

    public JarUploadResponse(@JsonProperty("filename") String filename, @JsonProperty("status") String status) {
        this.filename = filename;
        this.status = status;
    }

    public String getJarID() {
        String[] URIParts = this.filename.split("/");
        return URIParts[URIParts.length - 1];
    }
}
