package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.processor;

import de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.common.utils.FileReader;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class Run {

    private static final Logger LOG = Logger.getLogger(Run.class);
    private static final int MAX_EVENT_DELAY = 60;

    public static class EventFilterBolt implements FilterFunction<AdEvent> {

        @Override
        public boolean filter(AdEvent adEvent) throws Exception {

            return adEvent.getEvent_type().equals("view");
        }
    }

    public static class EventMapper implements MapFunction<AdEvent, Tuple2<String, Long>> {

        @Override
        public Tuple2<String, Long> map(AdEvent adEvent) throws Exception {

            return new Tuple2<>(adEvent.getAd_id(), adEvent.getEvent_time());
        }
    }

    public static final class RedisJoinBolt extends RichFlatMapFunction<Tuple2<String, Long>, Tuple3<String, String, Long>> {

        transient RedisAdCampaignCache redisAdCampaignCache;

        @Override
        public void open(Configuration parameters) {
            //initialize jedis
            ParameterTool parameterTool = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
            parameterTool.getRequired("redis.host");
            LOG.info("Opening connection with Jedis to " + parameterTool.getRequired("redis.host"));
            this.redisAdCampaignCache = new RedisAdCampaignCache(parameterTool.getRequired("redis.host"));
            this.redisAdCampaignCache.prepare();
        }

        @Override
        public void flatMap(Tuple2<String, Long> input, Collector<Tuple3<String, String, Long>> out) throws Exception {

            String ad_id = input.getField(0);
            String campaign_id = this.redisAdCampaignCache.execute(ad_id);
            if (campaign_id == null) {
                campaign_id = "UNKNOWN";
                //return;
            }

            Tuple3<String, String, Long> tuple = new Tuple3<>(campaign_id, input.getField(0), input.getField(1));
            out.collect(tuple);
        }
    }


    public static class CampaignProcessorV2 extends ProcessWindowFunction<Tuple3<String, String, Long>, Tuple3<String, Long, Long>, Tuple, TimeWindow> {

        private String redisServerHostname;

        @Override
        public void open(Configuration parameters) throws Exception {

            ParameterTool parameterTool = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
            parameterTool.getRequired("redis.host");
            LOG.info("Opening connection with Jedis to " + parameterTool.getRequired("redis.host"));

            redisServerHostname = parameterTool.getRequired("redis.host");
        }

        @Override
        public void process(Tuple tuple, Context context, Iterable<Tuple3<String, String, Long>> elements, Collector<Tuple3<String, Long, Long>> out) throws Exception {

            // get campaign key
            String campaign_id = tuple.getField(0);
            // count the number of ads for this campaign
            Iterator<Tuple3<String, String, Long>> iterator = elements.iterator();
            long count = 0;
            while (iterator.hasNext()) {
                count++;
                iterator.next();
            }
            // create output of operator, of course, there is nothing to consume this, but thats fine
            out.collect(new Tuple3<>(campaign_id, count, context.window().getEnd()));

            // write campaign id, the ad count, the timestamp of the window to redis
            Redis.execute(campaign_id, count, context.window().getEnd(), redisServerHostname);
        }
    }

    public static void main(final String[] args) throws Exception {

        // ensure checkpoint interval is supplied as an argument
        if (args.length != 1) {
            throw new IllegalStateException("Required Command line argument: [CHECKPOINT_INTERVAL]");
        }
        int interval = Integer.parseInt(args[0]);

        // retrieve properties from file
        Properties props = FileReader.GET.read("advertising.properties", Properties.class);

        // creating map for global properties
        Map<String, String> propsMap = new HashMap<>();
        for (final String name: props.stringPropertyNames()) {
            propsMap.put(name, props.getProperty(name));
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // setting global properties from file
        env.getConfig().setGlobalJobParameters(ParameterTool.fromMap(propsMap));

        env.disableOperatorChaining();

        // configuring RocksDB state backend to use HDFS
/*        String backupFolder = props.getProperty("hdfs.backupFolder");
        StateBackend backend = new RocksDBStateBackend(backupFolder, true);
        env.setStateBackend(backend);

        // start a checkpoint based on supplied interval
        env.enableCheckpointing(interval);*/

        // set mode to exactly-once (this is the default)
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // checkpoints have to complete within two minute, or are discarded
        env.getCheckpointConfig().setCheckpointTimeout(380000);

        // enable externalized checkpoints which are deleted after job cancellation
        env.getCheckpointConfig().enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);

        // allow job recovery fallback to checkpoint when there is a more recent savepoint
        env.getCheckpointConfig().setPreferCheckpointForRecovery(true);

        // setup Kafka consumer
        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.setProperty("bootstrap.servers", props.getProperty("kafka.brokers")); // Broker default host:port
        kafkaConsumerProps.setProperty("group.id", props.getProperty("kafka.consumer.group"));   // Consumer group ID
        kafkaConsumerProps.setProperty("auto.offset.reset", "earliest");                         // Always read topic from start

        FlinkKafkaConsumer<AdEvent> myConsumer =
            new FlinkKafkaConsumer<>(
                props.getProperty("kafka.consumer.topic"),
                new AdEventSchema(),
                kafkaConsumerProps);

        // configure event-time and watermarks
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setAutoWatermarkInterval(1000L);

        // assign a timestamp extractor to the consumer
        myConsumer.assignTimestampsAndWatermarks(new AdEventTSExtractor(MAX_EVENT_DELAY));

        // create direct kafka stream
        DataStream<AdEvent> messageStream =
            env.addSource(myConsumer)
                .name("DeserializeBolt")
                .setParallelism(Integer.parseInt(props.getProperty("kafka.partitions")));

        messageStream
            //Filter the records if event type is "view"
            .filter(new EventFilterBolt())
            .name("EventFilterBolt")
            // project the event
            .map(new EventMapper())
            .name("project")
            // perform join with redis data
            .flatMap(new RedisJoinBolt())
            .name("RedisJoinBolt")
            // process campaign
            .keyBy(0)
            //.flatMap(new CampaignProcessor())
            .timeWindow(Time.milliseconds(10000))
            .process(new CampaignProcessorV2())
            .name("CampaignProcessor");

        env.execute("Advertising");
    }
}
