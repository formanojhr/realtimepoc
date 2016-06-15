package monitoring.internal;

import com.timgroup.statsd.StatsDClient;
import monitoring.Metric;
import monitoring.internal.reporter.DefaultKafkaMetricsReporter;
import org.apache.kafka.common.metrics.KafkaMetric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sending gauges for kafka clients. At the moment it is for investigation only,
 * it should be decided what is useful for tracking.
 * 
 * @author milan.panic
 *
 */
public class KafkaStats implements Metric {

	private static final Map<String, DefaultKafkaMetricsReporter> metricsMap = new ConcurrentHashMap<String, DefaultKafkaMetricsReporter>();

	@Override
	public void logMetric(StatsDClient logger) {
		/*
		 * Internal kafka metrics are sent only if module is enabled and it is
		 * some relevant application that is using kafka(producer exists in core
		 * module, every module is able to send messages)
		 */
			for (String name : metricsMap.keySet()) {
				for (KafkaMetric kafkaMetric : metricsMap.get(name).getMetricsMap().values()) {
					logger.gauge("kafka.monitoring." + name + "." + kafkaMetric.metricName().name(), kafkaMetric.value());
				}
			}
	}
	
	public static void addMetricsReporter(DefaultKafkaMetricsReporter metricsReporter) {
		metricsMap.put(metricsReporter.getName(), metricsReporter);
	}
	
	public static void removeMetricsReporter(DefaultKafkaMetricsReporter metricsReporter) {
		metricsMap.remove(metricsReporter.getName());
		
	}


}
