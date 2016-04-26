package com.plantronics.data.storm.producer;

/**
 * Created by twang on 4/21/16.
 */

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class kafkaProducer
{
    private static final Logger logger = LoggerFactory.getLogger(kafkaProducer.class);
    private Producer<String, String> producer;
    private static final String CONVERSATION_DYNAMIC_DURATION = "250";

    public kafkaProducer(String brokerList, String zookeeper, String topicName)
    {

        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList);
        props.put("zk.connect", zookeeper);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        // Create the Producer Configuration
        ProducerConfig config = new ProducerConfig(props);

        // Create the Producer Instance
        producer = new Producer<String, String>(config);
    }

    public void PublishMessage(String topic, int ID)
    {

        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));//Specify your timezone. Here is PST
        try {

            Date CurrentDate;
            long msgUnixtime;
            String msgDatetime;

            Random r = new Random();
            double rangeMax = 30;
            double rangeMin = 10;
            double cdDuration = 0;
            DecimalFormat df = new DecimalFormat("#.####");

            String signal;

            for (int repeat = 0; repeat < 6; repeat++) {

                CurrentDate = new Date();
                msgUnixtime = CurrentDate.getTime();
                msgDatetime = dfm.format(CurrentDate);

                cdDuration = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                cdDuration = Double.valueOf(df.format(cdDuration));

                signal = ID + "," + msgDatetime + "," + msgUnixtime +
                                "," + CONVERSATION_DYNAMIC_DURATION + "," + cdDuration;

                KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, signal);

                logger.info("Sending messages to topic " + topic + ": " + signal);
                System.out.println("Sending messages to topic " + topic + ": " + signal);
                producer.send(msg);
                //Thread.sleep(100);
            }
        } catch (Exception e) {
            String errMsg = "Error generating data for each device.";
            logger.error(errMsg + ": " + e.toString());
        }

    }

    public static void main( String[] args )
    {
        String brokerList = args[0];
        String zookeeper = args[1];
        String topicName = args[2];

        logger.info("broker list:" + brokerList);
        logger.info("zk:" + zookeeper);
        logger.info("topicName:" + topicName);

        kafkaProducer producer = new kafkaProducer(brokerList, zookeeper, topicName);

        int deviceID = 0;
        while(deviceID < 20) {
            producer.PublishMessage(topicName, deviceID);
            deviceID++;
        }
    }
}


