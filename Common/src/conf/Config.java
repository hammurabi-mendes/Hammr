package conf;

import java.util.HashMap;
import java.util.Map;

public final class Config {
	public final static int DEFAULT_FILE_SERVER_PORT = 10000;
	public final static int DEFAULT_FILE_SERVER_BUFFER_SIZE = 4096;
	public static final int DEFAULT_SLOT_NUM = 2;
	// Basedir in the HDFS
	public static final String DEFAULT_HDFS_BASEDIR = "/Hammr";
	// Basedir in the local filesystem for launcher
	public static final String DEFAULT_LAUNCHER_BASEDIR = "/tmp";
	// schedule related
	public static final long DEFAULT_UNSCHEDULE_COST = 1 << 20;
	public static final long DEFAULT_PREEMPTION_COST = 1 << 20;
	// unit cost is cost of moving UNIT_COST bytes throught the cluster
	public static final double UNIT_COST = 1 << 20;
	public static final double DEFAULT_RACK_RATIO = 0.0;

	public static final short HDFS_REPLICATION = 1;
	public static final boolean PREEMPTION_ENABLE = false;
	public static final long STARVING_TIME_THRESHOLD = 30000;

	private static final Map<String, Double> poolWeights = new HashMap<String, Double>();
	public static final long PREEMPTION_INTERVAL_MILLS = 30000;
	public static final long SCHEDULE_INTERVAL = 1000;
	public static final long MAX_SCHEDULE_COST = 0;
	
	static {
		poolWeights.put("Hammurabi", 2.0);
		poolWeights.put("Rodrigo", 3.0);
	}

	public static Double getPoolWeight(String poolName) {
		return poolWeights.get(poolName);
	}
}
