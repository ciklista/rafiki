package de.tu_berlin.mpds.metric_collector.controller;

import de.tu_berlin.mpds.metric_collector.service.ExperimentRunner;
import java.io.IOException;
import java.sql.SQLException;
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
  public EndPointController(ExperimentRunner experimentRunner) {
    this.experimentRunner = experimentRunner;
  }

  @PostMapping(value = "/experiment" ,consumes = {"application/json"})
  public ResponseEntity<Object> startExperiment(@RequestBody String[] operators,
                                           @RequestParam(value = "jarid") String jarId,@RequestParam(value = "max") int maxParallelism
                                           ) throws  InterruptedException, ExecutionException, SQLException, IOException {

    experimentRunner.entrypoint(operators, jarId, maxParallelism);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/result")
  public ResponseEntity<Object> getResults(){

    // here we can have a method which queries the db and sends the objects to the web API.


    return ResponseEntity.status(HttpStatus.OK).build();

    //when we will have a response object we will use the bellow return statement.
    // return ResponseEntity.ok(the_object_what_we_want_to_send);
  }
}
