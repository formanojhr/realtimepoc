package com.plantronics.data.storm.producer;

import com.plantronics.data.storm.common.Constants;
import com.plantronics.data.storm.producer.events.SoundEvent;
import com.plantronics.data.storm.producer.events.SoundEventProfile;
import com.plantronics.data.storm.producer.events.impl.OverTalkWarningEventProfile;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import monitoring.internal.PerfLogger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.google.common.util.concurrent.AtomicDouble;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mramakrishnan on 4/21/16.
 */
public class ConversationDynamicProducer {
    private static final Logger logger = LoggerFactory.getLogger(kafkaProducer.class);
    private KafkaProducer<String, String> producer;

    private static final String CONVERSATION_DYNAMIC_DURATION = "250";
    private static final int DEFAULT_CONVERSATION_DYNAMIC_THROUGHPUT = 1000;
    private int throughput = DEFAULT_CONVERSATION_DYNAMIC_THROUGHPUT;
    private AtomicDouble currentThroughput;
    private String timeBetweenEvents = "1000";
    private final AtomicDouble testDuration;
    private String version = "1.0";
    private String deviceId = "";
    private DateTime dt;
    private DateTimeFormatter fmt;
    Random r = new Random();
    private int Low = 0;
    private int High = 3;
    SoundEventProfile soundEventProfile = null;
    private String[] deviceIdArr= new String[]{"12345524","12343212", "23124232", "32412323"};
    private int numEvents = 100;
    private final PerfLogger perfLogger;
    public static String PRODUCER_MESSAGE_COUNT="storm.load.test.kafka.producer.cd.count";

    public ConversationDynamicProducer(String brokerList, String zookeeper, String topicName, String throughput, String timePeriod) throws Exception {
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
        currentThroughput = new AtomicDouble(0);
        testDuration= new AtomicDouble(0);
        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new PrintStats(this.currentThroughput, this.testDuration), 0, 2000);
        this.numEvents = new Random().nextInt((1000 - 100) + 1) + 100; // range [100-1000];
        this.dt= new DateTime();
        this.fmt= ISODateTimeFormat.dateTime();
        soundEventProfile = new OverTalkWarningEventProfile();
        if(timePeriod !=null && timePeriod.isEmpty()){
            this.timeBetweenEvents=timePeriod;
        }
        this.perfLogger=new PerfLogger();
        perfLogger.init();
    }


    public void PublishMessage(String topic, int ID) throws InterruptedException {
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));//Specify your timezone. Here is PST

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
//            String signal = ID + "," + msgDatetime + "," + msgUnixtime + "," + CONVERSATION_DYNAMIC_DURATION + "," + nearEnd +
//                    "," + farEnd + "," + signalValue;
            String jsonString="";
            try {
                jsonString = generateCDJSONString();
            } catch (Exception e) {
                System.out.println("Error generating CD json:  " + e.getMessage());
                logger.error("Error generating CD json:", e);
            }

            KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, jsonString);

//            System.out.println("Sending Messge to topic " + topic + ": " + signal);
            logger.info("Sending Message to topic " + topic + ": " + jsonString);
            this.testDuration.set(testrunTime);
            this.currentThroughput.set(throughput_count / testrunTime);
            if (this.currentThroughput.get() >= (double) this.throughput) {// Needed throughput already reached.
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
            producer.send(new ProducerRecord<String, String>(topic, jsonString),
                    new ProducerCallback<String>(msg));
            perfLogger.getPerfLoggerInstance().count(PRODUCER_MESSAGE_COUNT,1);
            end = System.nanoTime();
            throughput_count++;
            this.currentThroughput.getAndAdd(1);
            testrunTime = testrunTime + (double) (end - start) / 1000000000.0;//convert to seconds from nano
//            System.out.println("Current throughput: " + this.throughput / testrunTime);
//            System.out.println("Test runtime " + testrunTime + "(s)");
        }
    }


    /**
     * Generate converstaion dynamic string and send it back string
     */
    private String generateCDJSONString() throws Exception {
        deviceId=deviceIdArr[r.nextInt(High-Low) + Low];
        logger.info("deviceId: "+deviceId);
        DateTimeFormatter patternFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        SoundEvent soundEvent = soundEventProfile.generateSoundEvent(Long.parseLong(timeBetweenEvents));

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("\"version\":\"");
        sb.append(version);
        sb.append("\",");

        sb.append("\"type\":\"");
        sb.append("conversationDynamic");
        sb.append("\",");

        sb.append("\""+ Constants.JSONFieldNames.TIME_STAMP+"\":");
        sb.append("\"");
        sb.append(fmt.print(dt));
        sb.append("\"");
        logger.debug("Timestamp: "+fmt.print(dt));
        sb.append(",");

        sb.append("\"deviceId\":\"");
        sb.append(deviceId);
        sb.append("\",");

        sb.append("\""+Constants.JSONFieldNames.TIME_PERIOD+"\":");
        sb.append(timeBetweenEvents);
        logger.info("timeBetweenEvents: "+ timeBetweenEvents );
        sb.append(",");

        sb.append("\""+Constants.JSONFieldNames.FAR_TALK_DURATION+"\":");
        sb.append(soundEvent.getFarEndDuration());
        sb.append(",");

        sb.append("\""+Constants.JSONFieldNames.NEAR_TALK_DURATION+"\":");
        sb.append(soundEvent.getNearEndDuration());
        sb.append(",");

        sb.append("\""+Constants.JSONFieldNames.DOUBLE_TALK_DURATION+"\":");
        sb.append(soundEvent.getOverTalkDuration());
        System.out.println(soundEvent.getOverTalkDuration());
        sb.append(",");

        sb.append("\"noTalkDuration\":");
        sb.append(soundEvent.getNoTalkDuration());
        sb.append(",");

        sb.append("\"farEndMaxDb\":");
        sb.append(soundEvent.getFarEndMaxDb());
        sb.append(",");

        sb.append("\"nearEndMaxDb\":");
        sb.append(soundEvent.getNearEndMaxDb());

        sb.append("}");

        return sb.toString();

    }


    public static void main(String[] args) {
        String brokerList = args[0];
        String zookeeper = args[1];
        String topicName = args[2];
        String throughput = args[3];
        String timePeriod = args[4];

        logger.info("broker list:" + brokerList);
        logger.info("zk:" + zookeeper);
        logger.info("topicName:" + topicName);
        logger.info("throughput requested:" + throughput);
        logger.info("time period for CD event" + timePeriod);
        ConversationDynamicProducer producer;
        try{
            producer = new ConversationDynamicProducer(brokerList, zookeeper, topicName, throughput,timePeriod);

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
        catch (Exception e){
            logger.info("Exception initializing ConversationDynamicProducer", e);
            System.exit(0);
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





