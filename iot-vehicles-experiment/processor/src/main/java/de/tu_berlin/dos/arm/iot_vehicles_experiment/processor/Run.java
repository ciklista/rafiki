package de.tu_berlin.dos.arm.iot_vehicles_experiment.processor;

import de.tu_berlin.dos.arm.iot_vehicles_experiment.common.events.Point;
import de.tu_berlin.dos.arm.iot_vehicles_experiment.common.events.TrafficEvent;
import de.tu_berlin.dos.arm.iot_vehicles_experiment.common.utils.FileReader;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.util.Properties;
import org.json.JSONObject;

public class Run {

    // traffic events are at most 60 sec out-of-order.
    private static final int MAX_EVENT_DELAY = 60;
    private static final Logger LOG = Logger.getLogger(Run.class);

    // class to filter traffic events within point of interest
    public static class POIFilter implements FilterFunction<TrafficEvent> {

        public final Point point;
        public final int radius;

        public POIFilter(Point point, int radius) {

            this.point = point;
            this.radius = radius;
        }

        @Override
        public boolean filter(TrafficEvent event) throws Exception {

            // Use Geodesic Inverse function to find distance in meters
            GeodesicData g1 = Geodesic.WGS84.Inverse(
                point.lt,
                point.lg,
                event.getPt().lt,
                event.getPt().lg);
            // determine if it is in the radius of the POE or not
            return g1.s12 <= radius;
        }
    }

    // Window to aggregate traffic events and calculate average speed in km/h
    public static class AvgSpeedWindow extends ProcessWindowFunction<TrafficEvent, Tuple5<Long, String, Float, Float, Integer>, String, TimeWindow> {

        public final int updateInterval;

        public AvgSpeedWindow(int updateInterval) {

            this.updateInterval = updateInterval;
        }

        @Override
        public void process(
                String vehicleId, Context context, Iterable<TrafficEvent> events,
                Collector<Tuple5<Long, String, Float, Float, Integer>> out) {

            Point previous = null;
            double distance = 0;
            int count = 0;
            for (TrafficEvent event : events) {
                if (previous != null) {
                    GeodesicData g1 = Geodesic.WGS84.Inverse(
                        previous.lt,
                        previous.lg,
                        event.getPt().lt,
                        event.getPt().lg);
                    distance += g1.s12;
                    count++;
                }
                previous = event.getPt();
            }
            // calculate time in hours
            double time = (count * updateInterval) / 3600000d;
            int avgSpeed = 0;
            if (time != 0) avgSpeed = (int) ((distance/1000) / time);
            assert previous != null;
            out.collect(new Tuple5<>(context.window().getEnd(), vehicleId, previous.lt, previous.lg, avgSpeed));
        }
    }

    // filter to determine if traffic vehicle is traveling over the speed limit
    public static class SpeedingFilter implements FilterFunction<Tuple5<Long, String, Float, Float, Integer>> {

        public final int speedLimit;

        public SpeedingFilter(int speedLimit) {

            this.speedLimit = speedLimit;
        }

        @Override
        public boolean filter(Tuple5<Long, String, Float, Float, Integer> trafficVehicle) throws Exception {

            return trafficVehicle.f4 >= speedLimit;
        }
    }

    // Retrieve vehicle type from database and parse json, builder is parsed to stop serialization error
    public static class VehicleEnricher extends RichMapFunction<Tuple5<Long, String, Float, Float, Integer>, String> {


        public VehicleEnricher() {

        }

        @Override
        public String map(Tuple5<Long, String, Float, Float, Integer> notification) throws Exception {

            return "{ timestamp: " + notification.f0 +
                    ", licensePlate: '" + notification.f1 + "'" +
                    ", latitude: " + notification.f2 +
                    ", longitude: " + notification.f3 +
                    ", avgSpeed: " + notification.f4 + "}";
        }
    }

    public static void main(String[] args) throws Exception {

        // ensure checkpoint interval is supplied as an argument
        if (args.length != 2) {
            throw new IllegalStateException("Required Command line argument: [PARALLELISM_CONFIGURATIONS] [CHECKPOINT_INTERVAL]");
        }
        int interval = Integer.parseInt(args[1]);

        // retrieve properties from file
        Properties props = FileReader.GET.read("processor.properties", Properties.class);
        int updateInterval = Integer.parseInt(props.getProperty("traffic.updateInterval"));
        int speedLimit = Integer.parseInt(props.getProperty("traffic.speedLimit"));
        int windowSize = Integer.parseInt(props.getProperty("traffic.windowSize"));

        JSONObject parallelismConfig = new JSONObject(args[0]);
        String[] requiredParallelismParameters = {
                props.getProperty("operator.kafkaSource.name"),
                props.getProperty("operator.poiFilter.name"),
                props.getProperty("operator.window.name"),
                props.getProperty("operator.speedFilter.name"),
                props.getProperty("operator.vehicleEnricher.name"),
                props.getProperty("operator.kafkaSink.name")
        };
        for (String requiredParallelismParameter : requiredParallelismParameters) {
            if (!parallelismConfig.has(requiredParallelismParameter)) {
                throw new IllegalStateException("Required parallelism parameter missing: " + requiredParallelismParameter);
            }
        }

        // setup Kafka consumer
        Properties kafkaConsumerProps = new Properties();

        kafkaConsumerProps.setProperty("bootstrap.servers", props.getProperty("kafka.brokers")); // Broker default host:port
        kafkaConsumerProps.setProperty("group.id", props.getProperty("kafka.consumer.group"));   // Consumer group ID
        kafkaConsumerProps.setProperty("auto.offset.reset", "earliest");                           // Always read topic from start

        FlinkKafkaConsumer<TrafficEvent> myConsumer =
            new FlinkKafkaConsumer<>(
                props.getProperty("kafka.consumer.topic"),
                new TrafficEventSchema(),
                kafkaConsumerProps);

        // setup Kafka producer
        Properties kafkaProducerProps = new Properties();
        kafkaProducerProps.setProperty("bootstrap.servers", props.getProperty("kafka.brokers")); // Broker default host:port
        kafkaProducerProps.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,"900000");
        kafkaProducerProps.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"my-transaction");
        kafkaProducerProps.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        FlinkKafkaProducer<String> myProducer =
            new FlinkKafkaProducer<>(
                props.getProperty("kafka.producer.topic"),
                (KafkaSerializationSchema<String>) (notification, aLong) -> {
                    return new ProducerRecord<>(props.getProperty("kafka.producer.topic"), notification.getBytes());
                },
                kafkaProducerProps,
                Semantic.EXACTLY_ONCE);

        // Start configurations ****************************************************************************************

        // set up streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Disable Operator chaining for fine grain monitoring
        env.disableOperatorChaining();

        // configuring RocksDB state backend to use HDFS
/*        String backupFolder = props.getProperty("hdfs.backupFolder");
        StateBackend backend = new RocksDBStateBackend(backupFolder, true);
        env.setStateBackend(backend);*/

        // start a checkpoint based on supplied interval
        env.enableCheckpointing(interval);

        // set mode to exactly-once (this is the default)
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // make sure 500 ms of progress happen between checkpoints
        //env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        // checkpoints have to complete within two minute, or are discarded
        env.getCheckpointConfig().setCheckpointTimeout(380000);

        // no external services which could take some time to respond, therefore 1
        // allow only one checkpoint to be in progress at the same time
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(10);

        // enable externalized checkpoints which are deleted after job cancellation
        env.getCheckpointConfig().enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);

        // allow job recovery fallback to checkpoint when there is a more recent savepoint
        env.getCheckpointConfig().setPreferCheckpointForRecovery(true);

        //env.getCheckpointConfig().setFailOnCheckpointingErrors();

        // End configurations ******************************************************************************************

        // configure event-time and watermarks
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setAutoWatermarkInterval(1000L);

        // assign a timestamp extractor to the consumer
        myConsumer.assignTimestampsAndWatermarks(new TrafficEventTSExtractor(MAX_EVENT_DELAY));

        // create direct kafka stream
        DataStream<TrafficEvent> trafficEventStream =
            env.addSource(myConsumer)
                .name(props.getProperty("operator.kafkaSource.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.kafkaSource.name")));

        // Point of interest
        Point point = new Point(52.51623f, 13.38532f); // centroid

        DataStream<String> trafficNotificationStream =
            trafficEventStream
                .filter(new POIFilter(point, 1000))
                .name(props.getProperty("operator.poiFilter.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.poiFilter.name")))
                .keyBy(TrafficEvent::getLp)
                .timeWindow(Time.milliseconds(windowSize))
                .process(new AvgSpeedWindow(updateInterval))
                .name(props.getProperty("operator.window.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.window.name")))
                .filter(new SpeedingFilter(speedLimit))
                .name(props.getProperty("operator.speedFilter.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.speedFilter.name")))
                .map(new VehicleEnricher())
                .name(props.getProperty("operator.vehicleEnricher.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.vehicleEnricher.name")));
                //.startNewChain();

        // write notifications to kafka
        myProducer.setWriteTimestampToKafka(true);
        trafficNotificationStream
            .addSink(myProducer)
                .name(props.getProperty("operator.kafkaSink.name"))
                .setParallelism((Integer) parallelismConfig.get(props.getProperty("operator.kafkaSink.name")));

        env.execute("Traffic");
    }
}
