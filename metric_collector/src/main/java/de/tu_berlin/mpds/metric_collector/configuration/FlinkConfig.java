package de.tu_berlin.mpds.metric_collector.configuration;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FlinkConfig {

  //we will change this configuration class by mapping it to properties file.
  public static final String BASE_URL_FLINK = "http://35.246.184.125:30881";

}
