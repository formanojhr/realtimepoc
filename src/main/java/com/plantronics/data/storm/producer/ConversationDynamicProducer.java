package com.plantronics.data.storm.producer;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mramakrishnan on 4/21/16.
 */
public class ConversationDynamicProducer {
    private static final Logger logger = LoggerFactory.getLogger(kafkaProducer.class);
    private KafkaProducer<String, String> producer;

    private static final String CONVERSATION_DYNAMIC_DURATION = "250";
    private static final int DEFAULT_CONVERSATION_DYNAMIC_THROUGHPUT = 1000;
    private int throughput = DEFAULT_CONVERSATION_DYNAMIC_THROUGHPUT;
    private AtomicDouble currentThrougput;
    private final AtomicDouble testDuration;
    public ConversationDynamicProducer(String brokerList, String zookeeper, String topicName, String throughput) {
        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList);
        props.put("zk.connect", zookeeper);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");
        props.put("bootstrap.servers", brokerList);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        try {
            this.throughput = Integer.parseInt(throughput);
        } catch (Exception e) {
            this.throughput = DEFAULT_CONVERSATION_DYNAMIC_THROUGHPUT;
        }

        //props.put("partitioner.class", "com.teng.iot.kafka.partitioner.SimplePartitioner");
        //kafka.producer.DefaultPartitioner: based on the hash of the key

        // Create the Producer Configuration
        ProducerConfig config = new ProducerConfig(props);

        // Create the Producer Instance
        producer = new KafkaProducer<String, String>(props);
        currentThrougput= new AtomicDouble(0);
        testDuration= new AtomicDouble(0);
        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new PrintStats(this.currentThrougput, this.testDuration), 0, 2000);
    }


    public void PublishMessage(String topic, int ID) throws InterruptedException {
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));//Specify your timezone. Here is PST

        Date CurrentDate = new Date();
        long msgUnixtime = System.nanoTime() / 10;
        String msgDatetime = dfm.format(new Date());


        // Create the message
        //Version 1: use dafault partition method:
        //KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, signal);

        //Version 2: use customized partition method:
        //Here we use ID as the information for partitioner.
        //(topic: String, key: K, message: V)
        int max = 30;
        int min = 0;

        Random r = new Random();
        int throughput_count = 0;
        double testrunTime = 0;
        long start;
        long end;
        int random_seed = 1;
        double currentThroughput = 0;
        while (true) {
            start = System.nanoTime();
            int nearEnd = r.nextInt((max - min) + 1) + min;
            int farEnd = r.nextInt((max - min) + 1) + min;

            Double signalValue = ID + 0.5 + (random_seed + 6);
            random_seed++;
            //17,2016-04-21 14:39:52,1461278392,
            //nearEnd: dB of the person who is talking
            //farEnd: dB of the person who replies.
            //Duration:
            String signal = ID + "," + msgDatetime + "," + msgUnixtime + "," + CONVERSATION_DYNAMIC_DURATION + "," + nearEnd +
                    "," + farEnd + "," + signalValue;

            KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, signal);

//            System.out.println("Sending Messge to topic " + topic + ": " + signal);
            logger.info("Sending Messge to topic " + topic + ": " + signal);
            this.testDuration.set(testrunTime);
            this.currentThrougput.set(throughput_count / testrunTime);
            if (currentThrougput.get() >= (double) this.throughput) {// Needed throughput already reached.
                logger.info("Hit the throughput needed " + this.throughput + " waiting to for 500 ms...");
                System.out.println("Hit the throughput needed " + this.throughput + " waiting to for 500 ms...");
//                System.out.println("Current throughput: " + currentThroughput + " waiting to for 500 ms...");
//                System.out.println("Test runtime " + testrunTime + "(s)");
                Thread.sleep((long) (500));
//                testrunTime = 0;//re init the remaining time
//                throughput_count=0;
            }
//            if (testrunTime >= 1000) {
//                throughput_count = 0;//reset throughput counter
//            }
            // Publish the message
//            producer.send(msg);
            producer.send(new ProducerRecord<String, String>(topic, signal),
                    new ProducerCallback<String>(msg));
            end = System.nanoTime();
            throughput_count++;
            this.currentThrougput.getAndAdd(1);
            testrunTime = testrunTime + (double) (end - start) / 1000000000.0;//convert to seconds from nano
//            System.out.println("Current throughput: " + this.throughput / testrunTime);
//            System.out.println("Test runtime " + testrunTime + "(s)");
        }
    }


    public static void main(String[] args) {
        String brokerList = args[0];
        String zookeeper = args[1];
        String topicName = args[2];
        String throughput = args[3];

        logger.info("broker list:" + brokerList);
        logger.info("zk:" + zookeeper);
        logger.info("topicName:" + topicName);
        logger.info("throughput requested:" + throughput);
        ConversationDynamicProducer producer = new ConversationDynamicProducer(brokerList, zookeeper, topicName, throughput);

        int counter = 0;
        try {
            while (counter < 21) {
                producer.PublishMessage(topicName, counter);
                counter++;
                Thread.sleep(10);
            }
        } catch (Exception e) {
            String errMsg = "Error generating data.";
            logger.error(errMsg + ": " + e.toString());
        }
    }

}

    class PrintStats extends TimerTask {
        AtomicDouble throughput;
        AtomicDouble testTime;
        private static final Logger logger = LoggerFactory.getLogger(PrintStats.class);
        public PrintStats(AtomicDouble throughput, AtomicDouble testTime) {
            this.throughput=throughput;
            this.testTime=testTime;
        }

        public void run() {
            logger.info("Current throughput: "+ throughput.get());
            System.out.println("Current throughput: " + throughput.get());
            logger.info("Current test duration: "+ testTime.get());
            System.out.println("Current test duration: " + testTime.get() + "(s)");
        }
    }





