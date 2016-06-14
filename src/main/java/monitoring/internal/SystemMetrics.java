package monitoring.internal;

public enum SystemMetrics {

	/**
	 * The amount of used memory in kilobytes.
	 */
	HEAP_MEMORY_USAGE("jvm.system.memory.heap.used"),

	/**
	 * The amount of memory in kilobytes that is committed for
	 * the Java virtual machine to use.  This amount of memory is
	 * guaranteed for the Java virtual machine to use.
	 */
	HEAP_MEMORY_COMMITTED("jvm.system.memory.heap.committed"),

	/**
	 * The maximum amount of memory in kilobytes that can be
	 * used for memory management
	 */
	HEAP_MEMORY_MAX("jvm.system.memory.heap.max"),

	/**
	 * The total number of major collections that have occurred.
	 * This method returns -1 if the collection count is undefined for this collector.
	 */
	HEAP_GC_COLLECTION_COUNT("jvm.system.gc.%s.collections.count"),

	/**
	 * The approximate accumulated for major collection elapsed time in milliseconds.
	 */
	HEAP_GC_COLLECTION_TIME("jvm.system.gc.%s.collections.time"),

	/**
	 * The peak count of JVM threads after start of the JVM
	 */
	THREAD_PEAK("jvm.system.threads.count.peak"),

	/**
	 * The active number of threads at this point.
	 */
	THREAD_COUNT("jvm.system.threads.count.active"),
	/**
	 * The jvm process CPU time.
	 */
	JVM_CPU_TIME("jvm.system.cpu.time"),
	/**
	 * Returns the "recent cpu usage" for the Java Virtual Machine process. This value is a double in the [0.0,1.0] interval.
	 * A value of 0.0 means that none of the CPUs were running threads from the JVM process during the recent period of time observed, while a value of 1.0 means
	 * that all CPUs were actively running threads from the JVM 100% of the time during the recent period being observed. Threads from the JVM include the application
	 * threads as well as the JVM internal threads. All values betweens 0.0 and 1.0 are possible depending of the activities going on in the JVM process and the
	 * whole system. If the Java Virtual Machine recent CPU usage is not available, the method returns a negative value.
	 */
	JVM_CPU_LOAD("jvm.system.cpu.load"),
	/**
	 * Returns the "recent cpu usage" for the whole system. This value is a double in the [0.0,1.0] interval. A value of 0.0 means that all CPUs were idle during the recent period of time observed,
	 * while a value of 1.0 means that all CPUs were actively running 100% of the time during the recent period being observed.
	 * All values between 0.0 and 1.0 are possible depending of the activities going on in the system. If the system recent cpu usage is not available,
	 * the method returns a negative value.
	 */
	SYSTEM_CPU_LOAD("host.system.cpu.load"),
	/**
	 * The amount of free physical memory in bytes.
	 */
	SYSTEM_FREE_MEMORY("host.system.memory.free")
	;

	private final String key;

	private SystemMetrics(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
