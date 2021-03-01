package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.processor;

import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Redis {

    public static class Connector implements Closeable {

        private static Connector self = null;
        private Jedis jedis;
        private LRUHashMap<Long, HashMap<String, Window>> campaign_windows;

        private Connector(String redisServerHostname) {

            this.jedis = new Jedis(redisServerHostname);
            this.campaign_windows = new LRUHashMap<>(10);
        }

        private void writeWindow(String campaign, long count, long timestamp) {

            String windowUUID = jedis.hmget(campaign, "" + timestamp).get(0);
            if (windowUUID == null) {
                windowUUID = UUID.randomUUID().toString();
                jedis.hset(campaign, Long.toString(timestamp), windowUUID);

                String windowListUUID = jedis.hmget(campaign, "windows").get(0);
                if (windowListUUID == null) {
                    windowListUUID = UUID.randomUUID().toString();
                    jedis.hset(campaign, "windows", windowListUUID);
                }
                jedis.lpush(windowListUUID, Long.toString(timestamp));
            }
            synchronized (campaign_windows) {
                jedis.hset(windowUUID, "seen_count", "" + count);
            }
            jedis.hset(windowUUID, "time_updated", Long.toString(System.currentTimeMillis()));
            jedis.lpush("time_updated", Long.toString(System.currentTimeMillis()));
        }

        public static synchronized Connector getInstance(String redisServerHostname) {
            if (self == null) self = new Connector(redisServerHostname);
            return self;
        }

        @Override
        public void close() throws IOException {

            this.jedis.close();
        }
    }

    public static void execute(String campaign_id, long count, long timestamp, String redisServerHostname) {

        Connector connector = Connector.getInstance(redisServerHostname);
        connector.writeWindow(campaign_id, count, timestamp);
    }
}
