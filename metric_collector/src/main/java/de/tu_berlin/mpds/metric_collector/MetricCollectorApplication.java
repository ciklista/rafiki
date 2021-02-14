package de.tu_berlin.mpds.metric_collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MetricCollectorApplication {

	public static void main(String[] args){
		SpringApplication.run(MetricCollectorApplication.class, args);
	}

}
