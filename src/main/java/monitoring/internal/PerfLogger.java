package monitoring.internal;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;
import org.apache.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The singleton instance of this class's {@link #statsDClient} may be obtained
 * by calling {@link PerfLogger#getPerfLoggerInstance()}.
 * <p>
 * Usage:
 * </p>
 * 
 * <pre>
 * PerfLogger.getPerfLoggerInstance().count("foo.counter", 1);
 * Initialization sequence:
 * Spring Context -> {a href file://cs-core/src/main/resources/core-context.xml} ->
 * perfLogger (bean initialization) -> INSTANCE(Singleton)
 * </pre>
 * 
 * <h3>Supported metric types:</h3>
 * <dl>
 * <dt>Counter</dt>
 * <dd>Tracks occurrences of an event. Used with
 * PerfLogger.getPerfLoggerInstance().count(String metricName, long count)</dd>
 * <dt>Gauge</dt>
 * <dd>Tracks value of a variable over time. May be updated directly. Used with
 * PerfLogger.getPerfLoggerInstance() .recordGaugeValue(String, Double).</dd>
 * or incrementally (
 * {@code PerfLogger.getPerfLoggerInstance().recordGaugeDelta(String metricName, long delta)}
 * ).
 * <dt>Timer</dt>
 * <dd>Measures elapsed time of an event. (
 * {@code PerfLogger.getPerfLoggerInstance().recordExecutionTime(String aspect, long timeInMs)}
 * )</dd>
 * <dt>Set</dt>
 * <dd>Tracks presence of a value when an event occurs. Used with
 * {@code PerfLogger.getPerfLoggerInstance().recordSetEvent(String metricName, String eventName)}
 * .</dd>
 * </dl>
 * 
 * @author manoj.ramakrishnan@plantronics.com
 */
public class PerfLogger implements Serializable{
	private static AtomicReference<PerfLogger> INSTANCE = new AtomicReference<PerfLogger>();
	private static StatsDClient statsDClient;
	private static final Logger log = Logger.getLogger(PerfLogger.class);
	protected Properties statsDConfig;
	private static final String PROPERTY_FILE="statsd.properties";
	
	public PerfLogger() {
		INSTANCE.getAndSet(this);
	}

	public synchronized void init() throws Exception{
		statsDConfig = new Properties();
		try {
			statsDConfig.load(ClassLoader.getSystemResourceAsStream(PROPERTY_FILE));
		} catch (FileNotFoundException e) {
			log.error("Encountered FileNotFoundException while reading configuration properties: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			log.error("Encountered IOException while reading configuration properties: " + e.getMessage());
			throw e;
		}
		if (Boolean.parseBoolean(statsDConfig.getProperty("statsd.enabled"))) {
			try {
				log.info("Statsd enabled in configuration. Starting the client..");
				this.statsDClient = new NonBlockingStatsDClient(statsDConfig.getProperty("statsd.prefix"),statsDConfig.getProperty("statsd.server.host"),
						Integer.parseInt(statsDConfig.getProperty("statsd.server.port")));
				log.info("Started statsd client on host:port " + statsDConfig.getProperty("statsd.server.host") + ":"
						+ statsDConfig.getProperty("statsd.server.port"));
			} catch (StatsDClientException e) {
				log.warn("Failed initializing  statsd client:" + e.getMessage());
				// initialize the NO-OP logger and keep moving the application.
				this.statsDClient = NoOpLogger.getInstance();
			}
		} else {
			// statsd disabled configuration
			log.warn("Statsd disabled in configuration. Metrics will not be pushed to StatsD.");
			// initialize the NO-OP logger and keep moving the application.
			this.statsDClient = NoOpLogger.getInstance();
		}
	}

	/**
	 * An accessor method to the singleton statsd client.
	 *
	 * @return NonBlockingStatsDClient
	 */
	public static StatsDClient getPerfLoggerInstance() {
		return PerfLogger.INSTANCE.get().statsDClient;
	}

}
