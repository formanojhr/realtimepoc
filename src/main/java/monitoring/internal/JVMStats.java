package monitoring.internal;

import com.timgroup.statsd.StatsDClient;
import monitoring.Metric;

import java.lang.management.*;
import java.util.concurrent.TimeUnit;

/**
 * Exposes JVM and system metrics using JMX. Most of these metrics are Gauges{@link StatsDClient} because of
 * the nature of the metrics.
 * All other metrics which need to be sampled and pushed periodically to Statsd backend should follow the
 * interface {@link Metric} this class implements.
 * @author manoj.ramakrishnan@plantronics.com
 */
public class JVMStats implements Metric {
    @Override
    public void logMetric(StatsDClient perfLog) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        perfLog.recordGaugeValue(SystemMetrics.THREAD_COUNT.getKey(), threadMXBean.getThreadCount());
        perfLog.recordGaugeValue(SystemMetrics.THREAD_PEAK.getKey(), threadMXBean.getPeakThreadCount());

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        if(operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean){
            perfLog.recordGaugeValue(SystemMetrics.JVM_CPU_TIME.getKey(), TimeUnit.MILLISECONDS.convert(((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).
                    getProcessCpuTime(),TimeUnit.NANOSECONDS));//convert to millis from nanos
            perfLog.recordGaugeValue(SystemMetrics.SYSTEM_CPU_LOAD.getKey(), ((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).getSystemCpuLoad());
            perfLog.recordGaugeValue(SystemMetrics.JVM_CPU_LOAD.getKey(), ((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).getProcessCpuLoad());
            perfLog.recordGaugeValue(SystemMetrics.SYSTEM_FREE_MEMORY.getKey(), ((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).getFreePhysicalMemorySize() >> 10); // convert to kb from bytes
        }
        // converting all the metrics into KB so it is quite unlikely the memory
        // will ever go beyond allowed max value, so it is safe to cast
        // long to integer on all these metrics.
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        perfLog.recordGaugeValue(SystemMetrics.HEAP_MEMORY_USAGE.getKey(), (int) memoryMXBean.getHeapMemoryUsage().getUsed() >> 10);
        perfLog.recordGaugeValue(SystemMetrics.HEAP_MEMORY_COMMITTED.getKey(), (int) memoryMXBean.getHeapMemoryUsage().getCommitted() >> 10);
        perfLog.recordGaugeValue(SystemMetrics.HEAP_MEMORY_MAX.getKey(), (int) memoryMXBean.getHeapMemoryUsage().getMax() >> 10);

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            perfLog.recordGaugeValue(String.format(SystemMetrics.HEAP_GC_COLLECTION_COUNT.getKey(), gcBean.getName()), (int) gcBean.getCollectionCount());
            perfLog.recordGaugeValue(String.format(SystemMetrics.HEAP_GC_COLLECTION_TIME.getKey(), gcBean.getName()), (int) gcBean.getCollectionTime());
        }
    }

}
