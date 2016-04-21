package com.plantronics.data.storm.producer;

/**
 * Created by twang on 4/21/16.
 */

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class kafkaProducer
{
    private static final Logger logger = LoggerFactory.getLogger(kafkaProducer.class);
    private Producer<String, String> producer;

    public kafkaProducer(String brokerList, String zookeeper, String topicName)
    {

        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList);
        props.put("zk.connect", zookeeper);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        //props.put("partitioner.class", "com.teng.iot.kafka.partitioner.SimplePartitioner");
        //kafka.producer.DefaultPartitioner: based on the hash of the key

        // Create the Producer Configuration
        ProducerConfig config = new ProducerConfig(props);

        // Create the Producer Instance
        producer = new Producer<String, String>(config);
    }

    public void PublishMessage(String topic, int ID)
    {

        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));//Specify your timezone. Here is PST

        Date CurrentDate = new Date();
        long msgUnixtime = CurrentDate.getTime()/1000;
        String msgDatetime = dfm.format(new Date());


        // Create the message
        //Version 1: use dafault partition method:
        //KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, signal);

        //Version 2: use customized partition method:
        //Here we use ID as the information for partitioner.
        //(topic: String, key: K, message: V)

        for (int repeat = 0; repeat < 6; repeat ++) {

            Double signalValue = ID + 0.5 + (repeat + 6);
            String signal = ID + "," + msgDatetime + "," + msgUnixtime + "," + signalValue;

            KeyedMessage<String, String> msg = new KeyedMessage<String, String>(topic, signal);

            System.out.println("Sending Messge to topic " + topic + ": " + signal );
            logger.info("Sending Messge to topic " + topic + ": " + signal );
            // Publish the message
            producer.send(msg);
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

        int counter = 0;
        try{
            while(counter < 21){
                producer.PublishMessage(topicName, counter);
                counter ++;
                Thread.sleep(10);
            }
        }catch (Exception e) {
            String errMsg = "Error generating data.";
            logger.error(errMsg + ": " + e.toString());
        }
    }
}


