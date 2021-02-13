package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.producer;

import akka.actor.AbstractActor;
import akka.actor.Props;
import de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.common.ads.AdEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ads {

    // for random event generation
    private static Random   rand = new Random();
    private static final String[] adTypes = {"banner", "modal", "sponsored-search", "mail", "mobile"};
    private static final String[] eventTypes = {"view", "click", "purchase"};
    public static final String[] campaignIds = makeIds(100);
    public static final String[] adIds = makeIds(10 * 100); // 10 ads per campaign
    private static final String[] userIds = makeIds(100);
    private static final String[] pageIds = makeIds(100);

    // returns an array of n uuids
    public static String[] makeIds(int n) {
        String[] uuids = new String[n];
        for (int i = 0; i < n; i++)
            uuids[i] = UUID.randomUUID().toString();
        return uuids;
    }

    static final Logger LOG = Logger.getLogger(Ads.class);

    public static class AdActor extends AbstractActor {

        public static AtomicInteger counter = new AtomicInteger(0);

        static Props props(int number, String topic, KafkaProducer<String, AdEvent> kafkaProducer) {

            return Props.create(AdActor.class, number, topic, kafkaProducer);
        }

        static final class Emit {

            int eventsCount;
            long creationDate;

            Emit(int eventsCount, long creationDate) {
                this.eventsCount = eventsCount;
                this.creationDate = creationDate;
            }
        }

        private String topic;
        private KafkaProducer<String, AdEvent> kafkaProducer;
        private int number;

        public AdActor(int number, String topic, KafkaProducer<String, AdEvent> kafkaProducer) {
            this.topic = topic;
            this.kafkaProducer = kafkaProducer;
            this.number = number;
        }

        @Override
        public Receive createReceive() {

            return receiveBuilder()
                .match(Emit.class, e -> {

                    if (this.number <= e.eventsCount) {
                        counter.incrementAndGet();
                        String userId = userIds[rand.nextInt(userIds.length)];
                        String pageId = pageIds[rand.nextInt(pageIds.length)];
                        String adId   = adIds[rand.nextInt(adIds.length)];
                        String adType = adTypes[rand.nextInt(adTypes.length)];
                        String eventType = eventTypes[rand.nextInt(eventTypes.length)];
                        String ipAddress = "1.2.3.4";

                        AdEvent adEvent = new AdEvent(e.creationDate, userId, pageId, adId, adType, eventType, ipAddress);
                        this.kafkaProducer.send(new ProducerRecord<>(this.topic, adEvent));
                    }
                })
                .matchAny(o -> LOG.error("received unknown message: " + o))
                .build();
        }
    }

}
