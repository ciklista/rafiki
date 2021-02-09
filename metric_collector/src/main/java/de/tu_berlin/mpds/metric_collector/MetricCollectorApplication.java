package de.tu_berlin.mpds.metric_collector;

import de.tu_berlin.mpds.metric_collector.service.PrometheusMetricService;
import de.tu_berlin.mpds.metric_collector.service.RequestMetricService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@EnableScheduling
public class MetricCollectorApplication {

	public static void main(String[] args){
		SpringApplication.run(MetricCollectorApplication.class, args);
	}
}
