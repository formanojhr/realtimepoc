package monitoring.internal;

import com.timgroup.statsd.StatsDClient;

/**
 * A no op logger for StatsD client.
 * 
 * @author mramakrishnan
 */
public class NoOpLogger implements StatsDClient {
	
	private static final NoOpLogger INSTANCE = new NoOpLogger();
	
	private NoOpLogger() {
		super();
	}
	
	@Override
	public void stop() {
		// do nothing
	}
	
	@Override
	public void count(String aspect, long delta) {
		// do nothing
	}
	
	@Override
	public void count(String aspect, long delta, double sampleRate) {
		// do nothing
	}
	
	@Override
	public void incrementCounter(String aspect) {
		// do nothing
	}
	
	@Override
	public void increment(String aspect) {
		// do nothing
	}
	
	@Override
	public void decrementCounter(String aspect) {
		// do nothing
	}
	
	@Override
	public void decrement(String aspect) {
		// do nothing
	}
	
	@Override
	public void recordGaugeValue(String aspect, long value) {
		// do nothing
	}
	
	@Override
	public void recordGaugeValue(String aspect, double value) {
		// do nothing
	}
	
	@Override
	public void recordGaugeDelta(String aspect, long delta) {
		
	}
	
	@Override
	public void recordGaugeDelta(String aspect, double delta) {
		// do nothing
	}
	
	@Override
	public void gauge(String aspect, long value) {
		// do nothing
	}
	
	@Override
	public void gauge(String aspect, double value) {
		// do nothing
	}
	
	@Override
	public void recordSetEvent(String aspect, String eventName) {
		
	}
	
	@Override
	public void set(String aspect, String eventName) {
		// do nothing
	}
	
	@Override
	public void recordExecutionTime(String aspect, long timeInMs) {
		// do nothing
	}
	
	@Override
	public void recordExecutionTime(String aspect, long timeInMs, double sampleRate) {
		// do nothing
	}
	
	@Override
	public void recordExecutionTimeToNow(String aspect, long systemTimeMillisAtStart) {
		// do nothing
	}
	
	@Override
	public void time(String aspect, long value) {
		// do nothing
	}

	public static StatsDClient getInstance() {
		return INSTANCE;
	}
}
