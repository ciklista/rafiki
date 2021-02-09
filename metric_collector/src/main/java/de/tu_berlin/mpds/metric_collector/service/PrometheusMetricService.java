package de.tu_berlin.mpds.metric_collector.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.PrometheusJsonResponse;
import de.tu_berlin.mpds.metric_collector.model.prometheusmetric.Result;
import de.tu_berlin.mpds.metric_collector.util.PrometheusQuery;
import io.mikael.urlbuilder.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class PrometheusMetricService {
    @Autowired
    private PrometheusQuery prometheusQuery;

    PrometheusJsonResponse executePrometheusQuery(URI uri, HttpClient client, ObjectMapper objectMapper) throws
            InterruptedException, ExecutionException, IOException {

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.get().body(), PrometheusJsonResponse.class);
    }

    Map<String, List<List<Double>>> getBackpressuredSubTasks(Integer daysToGoBack) throws InterruptedException, ExecutionException, IOException {
        // Key of the hashmap will be jobId_TaskId_SubtaskIndex

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1).with(LocalTime.of(0, 0));
        ZonedDateTime startDate = endDate.minusDays(daysToGoBack);

        Map<String, List<List<Double>>> resultMap = new HashMap<>();
        while (!startDate.isAfter(endDate)) {
            URI uri = prometheusQuery.getQUERY_BACKPRESSURE_BY_SUBTASK(startDate, startDate.plusDays(1), "10s");
            List<Result> queryResult = executePrometheusQuery(uri, client, objectMapper).getData().getResult();
            for (Result result : queryResult) {
                String key = result.getMetric().getJobId() + "_" +
                                result.getMetric().getTaskId() + "_" +
                                result.getMetric().getSubtaskIndex();

                if (!resultMap.containsKey(key)) {
                    List<List<Double>> initList = new ArrayList<>();
                    resultMap.put(key, initList);
                }
                for (List value : result.getValues()) {
                    Integer timestamp = (Integer) value.get(0);
                    Double metric = Double.parseDouble((String) value.get(1));
                    resultMap.get(key).add(List.of(timestamp.doubleValue(), metric));
                }


            }
            startDate = startDate.plusDays(1);
        }

        return resultMap;
    }

    Map<String, List<Double>> getMaxValuesFromRangeQueryByTaskId(String rangeQueryString, Integer daysToGoBack)
            throws InterruptedException, ExecutionException, IOException {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1).with(LocalTime.of(0, 0));
        ZonedDateTime startDate = endDate.minusDays(daysToGoBack);

        Map<String, List<Double>> max_values = new HashMap<>();
        while (!startDate.isAfter(endDate)) {
            URI uri = prometheusQuery.getQUERY_BACKPRESSURE_BY_SUBTASK(startDate, startDate.plusDays(1), "10s");
            List<Result> results = executePrometheusQuery(uri, client, objectMapper).getData().getResult();
            for (Result result : results) {
                String task_id = result.getMetric().getTaskId();
                List<List<Object>> values = result.getValues();
                if (!max_values.containsKey(task_id)) {
                    max_values.put(task_id, List.of(0.0, 0.0));
                }

                double max_value = max_values.get(task_id).get(1);

                for (List<Object> value : values) {
                    double castValue = Double.parseDouble((String) value.get(1));
                    if (castValue > max_value) {
                        Integer timestamp = (Integer) value.get(0);
                        max_values.put(task_id, List.of(timestamp.doubleValue(), castValue));
                        max_value = castValue;
                    }
                }


            }
            startDate = startDate.plusDays(1);
        }
        System.out.println("Found the following maximum values per second:");

        for (Map.Entry<String, List<Double>> entry : max_values.entrySet()) {
            System.out.println("TaskId:" + entry.getKey() + " Max value: " + entry.getValue().get(1).intValue() +
                    " at Timestamp " + entry.getValue().get(0).intValue());
        }

        return max_values;
    }
}


