# iot_vehicles_experiment

## Setup
Use [Maven](https://maven.apache.org) to create executable jars for the Kafka producer and Flink processor.

Experiments require Zookeeper, Kafka, HDFS, and Flink.

## Configuration

Configurations need to be specified for both the producer and processor.

Producer: producer/src/main/resources/producer.properties

Processor: processor/src/main/resources/processor.properties

After specifying the configurations, create jars for the producer and processor by running ``mvn clean && mvn package`` in the root directory

## Kafka producer

Events are generated according to the configured csv file in processor.properties

Start the producer with:

```bash
java -jar producer/target/producer-1.0-SNAPSHOT.jar
```

## Flink processor

The "processor-1.0-SNAPSHOT.jar" can be submitted to the Flink cluster via the Flink dashboard or by running:
```bash
flink run processor/target/processor-1.0-SNAPSHOT.jar
```
