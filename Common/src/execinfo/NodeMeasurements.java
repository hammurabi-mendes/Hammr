package execinfo;

import java.io.Serializable;

public class NodeMeasurements implements Serializable {
	private static final long serialVersionUID = 1L;

	private long realTime;
	private long cpuTime;
	private long userTime;

	public NodeMeasurements(long realTime, long cpuTime, long userTime) {
		this.realTime = realTime;
		this.cpuTime = cpuTime;
		this.userTime = userTime;
	}

	public long getRealTime() {
		return realTime;
	}

	public long getCpuTime() {
		return cpuTime;
	}

	public long getUserTime() {
		return userTime;
	}
}
