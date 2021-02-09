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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class PrometheusMetricService {
    @Autowired
    private PrometheusQuery prometheusQuery;

    PrometheusJsonResponse sendRequestToPrometheusForMetric(String urlWithQuery, HttpClient client, ObjectMapper objectMapper) throws
            InterruptedException, ExecutionException, IOException {

        HttpRequest request = HttpRequest.newBuilder(URI.create(urlWithQuery)).GET().build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.get().body(), PrometheusJsonResponse.class);
    }

    PrometheusJsonResponse sendRangeQuery(String queryString, ZonedDateTime start, ZonedDateTime end, String step, HttpClient client, ObjectMapper objectMapper) throws
            InterruptedException, ExecutionException, IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'");

        URI uri = UrlBuilder.fromString(prometheusQuery.getBaseUrlPrometheus())
                .withPath("api/v1/query_range")
                .addParameter("query", queryString)
                .addParameter("start", start.format(formatter))
                .addParameter("end", end.format(formatter))
                .addParameter("step", step)
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        ;

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.get().body(), PrometheusJsonResponse.class);
    }

    Map<String, List<Double>> getMaxValuesFromRangeQueryByTaskId(String rangeQueryString, Integer daysToGoBack)
            throws InterruptedException, ExecutionException, IOException {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1).with(LocalTime.of(0, 0));
        ZonedDateTime startDate = endDate.minusDays(daysToGoBack);

        Map<String, List<Double>> max_values = new HashMap<>();
        while (!startDate.isAfter(endDate)) {
            List<Result> results = sendRangeQuery(rangeQueryString,
                    startDate, startDate.plusDays(1), "10s", client, objectMapper).getData().getResult();
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


