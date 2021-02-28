package de.tu_berlin.mpds.metric_collector.controller;

import de.tu_berlin.mpds.metric_collector.model.experiments.ExperimentResults;
import de.tu_berlin.mpds.metric_collector.service.DatabaseService;
import de.tu_berlin.mpds.metric_collector.service.ExperimentRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndPointController {


    private final ExperimentRunner experimentRunner;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    public EndPointController(ExperimentRunner experimentRunner) {
        this.experimentRunner = experimentRunner;
    }

    @PostMapping(value = "/experiment", consumes = {"application/json"})
    public ResponseEntity<Object> startExperiment(@RequestBody String[] operators,
                                                  @RequestParam(value = "clusterAddress") String clusterAddress,
                                                  @RequestParam(value = "jarId") String jarId,
                                                  @RequestParam(value = "maxParallelism") int maxParallelism
    ) throws InterruptedException, ExecutionException, SQLException, IOException {
      clusterAddress = clusterAddress.replaceAll("/+$", "");
      experimentRunner.start(clusterAddress, operators, jarId, maxParallelism);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/result")
    public ResponseEntity<Object> getResults(@RequestParam(value = "jarId") String jarId)
            throws IOException, SQLException {

        List<ExperimentResults> results = databaseService.getExperimentResults(jarId);

        return ResponseEntity.ok(results);
    }
}
