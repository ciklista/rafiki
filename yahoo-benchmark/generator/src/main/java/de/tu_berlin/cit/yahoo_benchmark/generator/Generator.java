package de.tu_berlin.cit.yahoo_benchmark.generator;

import de.tu_berlin.cit.yahoo_benchmark.common.utils.Resources;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Generator {

	// default values
	public static int numCampaigns = 100;
	public static int timeDivisor = 10000; // 10 seconds

    //address of your redis server
	private static JedisPool pool;
	private static Jedis jedis;
	private static Producer<String, String> producer;
	
	// for random event generation
	private static Random   rand = new Random();
	private static String[] adTypes = {"banner", "modal", "sponsored-search", "mail", "mobile"};
	private static String[] eventTypes = {"view", "click", "purchase"};
	private static String[] campaignIds = makeIds(numCampaigns);
	private static String[] adIds = makeIds(10 * numCampaigns); // 10 ads per campaign
	private static String[] userIds = makeIds(100);
	private static String[] pageIds = makeIds(100);

	// returns an array of n uuids
	public static String[] makeIds(int n) {
		String[] uuids = new String[n];
		for (int i = 0; i < n; i++)
			uuids[i] = UUID.randomUUID().toString();
		return uuids;
	}

	
	// initialize jedis and kafka from config file
	private static void init() throws Exception {
		System.out.println("Initializing...");
		
        // retrieve properties from file
        Properties config = Resources.GET.read("advertising.properties", Properties.class);

        String kafkaBroker = config.getProperty("kafka.brokers");
        String redisHost = config.getProperty("redis.host");
        int redisPort = Integer.parseInt(config.getProperty("redis.port"));
        
		// jedis
		pool = new JedisPool(redisHost, redisPort);
		jedis = pool.getResource();
		
		// kafka producer
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaBroker);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer = new KafkaProducer<>(props);
		
	}
	
	
	public static void writeToRedis() {
		System.out.println("Writing campaign data to redis");
		
		// reset redis
		jedis.flushAll(); 
		
		// write campaign ids
		jedis.sadd("campaigns", campaignIds);
		
		// assign 10 ads to each campaign
		for (int i = 0; i < campaignIds.length; i++) {
			for (int j = 0; j < 10; j++) {
				jedis.set(adIds[i*10+j], campaignIds[i]);
			}
		}
		
	}
	
	
	public static JSONObject makeEvent(long time) {
						
		JSONObject event = new JSONObject();
		event.put("user_id", userIds[rand.nextInt(userIds.length)]);
		event.put("page_id", pageIds[rand.nextInt(pageIds.length)]);
		event.put("ad_id", adIds[rand.nextInt(adIds.length)]);
		event.put("ad_type", adTypes[rand.nextInt(adTypes.length)]);
		event.put("event_type", eventTypes[rand.nextInt(eventTypes.length)]);
		event.put("event_time", time);
		event.put("ip_address", "1.2.3.4");

		return event;
	}
	

	public static void run(int throughput, int duration) throws InterruptedException {
		
		System.out.println("Running, emitting " + throughput + " tuples per second for " + duration + " seconds");
		
		Long endTime = System.currentTimeMillis() + duration*1000;
		Long sendTime = System.currentTimeMillis();
		long offset = 1000 / throughput;
				
		// continue to run for duration
		while(true) {
			
			// send messages at configured throughput
			sendTime = sendTime + offset;
			Long cur = System.currentTimeMillis();
			if (sendTime > cur) {
				Thread.sleep(sendTime - cur);
			}
			
			// alert if falling behind
			if (cur > sendTime + 100) {
				System.out.println("Falling behind by " + (cur-sendTime) + "ms");
			}
			
			// send event to kafka
		    producer.send(new ProducerRecord<String, String>("ad-events", makeEvent(sendTime).toString()));
		}
		
		// done sending events
		//producer.close();
		
	}
	
		
	
	public static void getStats( ) {
		System.out.println("Getting stats...");
		
		
		try {
			FileWriter seen = new FileWriter("seen.txt", false);
			FileWriter updated = new FileWriter("updated.txt", false);
			
			Set<String> campaignIds = jedis.smembers("campaigns");
			for (String campaignId : campaignIds) {
				String windowsKey = jedis.hget(campaignId, "windows");
				Long windowCount = jedis.llen(windowsKey);
				List<String> windows = jedis.lrange(windowsKey, 0, windowCount);
				
				for (String windowTime : windows) {
					String windowKey = jedis.hget(campaignId, windowTime);
					String seenCount = jedis.hget(windowKey, "seen_count");
					String time_updated = jedis.hget(windowKey, "time_updated");
					// TODO: check if data written is correct
					seen.write(seenCount);
					updated.write(Long.toString((Long.parseLong(time_updated) - Long.parseLong(windowTime))) + "\n");
				}
			}
			
			seen.close();
			updated.close();
			jedis.close();
			pool.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.out.println("Usage: [throughput per second] [generator duration (s)]");
			return;
		}
		
		int throughput = Integer.parseInt(args[0]);
		int duration = Integer.parseInt(args[1]);
		
		// initialize jedis and kafka producer
		init();

		// write campaign and ad ids to redis
		//writeToRedis();
		
		// generate [throughput] tupels per second for [duration] seconds
		run(throughput, duration);
		
		// process remaining events
		Thread.sleep(10000);

		// calculate end-to-end latency
		//getStats();
		
		System.out.println("Done.");
		
	}

}
