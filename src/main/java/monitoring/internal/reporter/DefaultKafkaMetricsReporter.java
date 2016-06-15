package monitoring.internal.reporter;


import monitoring.internal.KafkaStats;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Platform extension of default kafka metrics for client, references on metrics
 * are kept in metricsMap. It should be decided what is interesting to be
 * tracked.
 * 
 * @author milan.panic
 *
 */
public class DefaultKafkaMetricsReporter implements MetricsReporter {
	
	private static final Object LOCK = new Object();
	private static final Map<String, KafkaMetric> metricsMap = new HashMap<String, KafkaMetric>();
	private String name;
	
	@Override
	public void configure(Map<String, ?> configs) {
		name = (String) configs.get("name");
		KafkaStats.addMetricsReporter(this);
	}
	
	@Override
	public void init(List<KafkaMetric> metrics) {
		synchronized (LOCK) {
			for (KafkaMetric kafkaMetric : metrics) {
				metricsMap.put(kafkaMetric.metricName().name(), kafkaMetric);
			}
		}
	}
	
	@Override
	public void metricChange(KafkaMetric metric) {
		synchronized (LOCK) {
			metricsMap.put(metric.metricName().name(), metric);
		}
	}
	
	@Override
	public void metricRemoval(KafkaMetric metric) {
		synchronized (LOCK) {
			metricsMap.remove(metric.metricName().name());
		}
	}
	
	@Override
	public void close() {
		KafkaStats.removeMetricsReporter(this);
	}
	
	public static Map<String, KafkaMetric> getMetricsMap() {
		return metricsMap;
	}
	
	public String getName() {
		return name;
	}
	
}
