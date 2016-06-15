package monitoring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import monitoring.internal.PerfLogger;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;

import com.timgroup.statsd.StatsDClient;

/**
 * Periodically collects metrics exposed as {@link Metric} services. An other
 * metrics which need to be sampled and pushed periodically to Statsd backend
 * should implement the interface {@link Metric} and register itself as a Spring
 * bean into the set {@link #metrics}. Example: {@link JVMStats
 * file://resources/core-context.xml}
 * 
 * @author manoj.ramamkrishnan@plantronics.com
 */

@ThreadSafe
public class PeriodicMetricCollector {
	
	private static final int DEFAULT_METRICS_HARVEST_TIME_INTERVAL = (int) TimeUnit.SECONDS.toSeconds(10);
	
	private static final long INITIAL_DELAY = TimeUnit.SECONDS.toSeconds(5);
	
	private static final StatsDClient perfLog = PerfLogger.getPerfLoggerInstance();// statsd
																					// UDP
																					// client

	private CopyOnWriteArraySet<Metric> metrics = new CopyOnWriteArraySet<Metric>();
	private static final String PROPERTY_FILE="statsd.properties";

	private static Logger log;
	
	private int metricsCollectionInterval = DEFAULT_METRICS_HARVEST_TIME_INTERVAL;
	
	private ScheduledExecutorService executorService;
	
	private Future<?> taskFuture;
	
	public CopyOnWriteArraySet<Metric> getMetrics() {
		return metrics;
	}

	protected Properties statsDConfig;
	
	public void setMetrics(CopyOnWriteArraySet<Metric> metrics) {
		this.metrics = metrics;
	}
	
	private final Runnable metricCollectionTask = new Runnable() {
		@Override
		public void run() {
			for (Metric metric : metrics) {
				try {
					if (Thread.interrupted()) {
						if (log.isDebugEnabled()) {
							log.debug("Metric collection interrupted.");
						}
						break;
					}
					metric.logMetric(perfLog);// callback to the metrics
												// register to log metrics
				} catch (Throwable t) {
					log.warn(String.format("Unexpected error while logging metric: %s", metric), t);
				}
			}
		}
	};
	
	/**
	 * An initialize method to start a periodic collection task for the
	 * executor. Condition: statsdEnabled set to true in application properties.
	 */
	public void start() {
		statsDConfig = new Properties();
		try {
			statsDConfig.load(ClassLoader.getSystemResourceAsStream(PROPERTY_FILE));
		} catch (FileNotFoundException e) {
			log.error("Encountered FileNotFoundException while reading configuration properties: " + e.getMessage());
		} catch (IOException e) {
			log.error("Encountered IOException while reading configuration properties: " + e.getMessage());
		}
		if (Boolean.parseBoolean(statsDConfig.getProperty("statsd.enabled"))) {
		// start the task and initialize executor if statsd is enabled in
		// application properties
			executorService = Executors.newScheduledThreadPool(2);
			update();
		}
	}
	
	/**
	 * Update method to construct a task with a metric collection sampling
	 * interval and an initial delay.
	 */
	public void update() {
		int harvestTimeInterval = getHarvestTimeInterval();
		
		if (harvestTimeInterval > 0) {
			if (harvestTimeInterval != metricsCollectionInterval && taskFuture != null) {
				taskFuture.cancel(false);
				taskFuture = null;
			}
			
			if (taskFuture == null)
				taskFuture = executorService
						.scheduleAtFixedRate(metricCollectionTask, INITIAL_DELAY, harvestTimeInterval, TimeUnit.SECONDS);
			log.info(String.format(
					"Periodic metrics collector service started and will collect the metrics periodically for every %d seconds.",
					harvestTimeInterval));
		} else {
			if (taskFuture != null) {
				taskFuture.cancel(false);
				taskFuture = null;
			}
			log.info("metricCollectionInterval enable property not set or is set to false. The service will not be started.");
		}
		metricsCollectionInterval = harvestTimeInterval;
	}
	
	private int getHarvestTimeInterval() {
//		int metricsCollectionTimeInterval = applicationProperties.getMetricCollectionInterval();
		return DEFAULT_METRICS_HARVEST_TIME_INTERVAL;
	}
	
	public void stop() {
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
		
		if (taskFuture != null) {
			taskFuture.cancel(true);
			taskFuture = null;
		}
	}
}
