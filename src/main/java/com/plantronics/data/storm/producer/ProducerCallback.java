package com.plantronics.data.storm.producer;


import kafka.producer.KeyedMessage;
import org.apache.hadoop.hive.ql.io.SymlinkTextInputFormat;
import org.apache.hadoop.hive.ql.log.PerfLogger;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that provides asynchronous information about message sent, and handling
 * message upon failure. If message fails, it is logged as failed message.
 * 
 * @author milan.panic
 *
 * @param <T>
 */
public class ProducerCallback<T> implements Callback {
	
	private final KeyedMessage<String, String> keyedMessage;
	private static final Logger logger = LoggerFactory.getLogger(ProducerCallback.class);
	public ProducerCallback(KeyedMessage<String, String> message) {
		keyedMessage=message;
	}

	@Override
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		if (exception != null) {
			logger.error("Kafka producer was unable to send message with id={}. Reason: {}. Message has been logged.", keyedMessage.message()
					, exception);
			System.out.println("Kafka producer was unable to send message with id={}. Reason: {}. Message has been logged."+ exception.getCause());
		}
	}
}
