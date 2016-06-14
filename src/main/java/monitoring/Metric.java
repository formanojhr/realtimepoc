package monitoring;

import com.timgroup.statsd.StatsDClient;

/**
 * Interface for performance metrics. Allows for metric collection
 * at caller's convenience (e.g., periodic schedule).
 */
public interface Metric {
    void logMetric(StatsDClient logger);
}

