package execinfo;

import java.io.Serializable;

/**
 * This class packages the whole set of node measurements.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class NodeMeasurements implements Serializable {
	private static final long serialVersionUID = 1L;

	private long realTime;
	private long cpuTime;
	private long userTime;

	/**
	 * Constructor method.
	 * 
	 * @param realTime Real time associated with the Node run.
	 * @param cpuTime CPU time associated with the Node run.
	 * @param userTime User time associated with the Node run.
	 */
	public NodeMeasurements(long realTime, long cpuTime, long userTime) {
		this.realTime = realTime;
		this.cpuTime = cpuTime;
		this.userTime = userTime;
	}

	/**
	 * Getter for the real time associated with the Node run.
	 * 
	 * @return The real time associated with the Node run.
	 */
	public long getRealTime() {
		return realTime;
	}

	/**
	 * Getter for the CPU time associated with the Node run.
	 * 
	 * @return The CPU time associated with the Node run.
	 */
	public long getCpuTime() {
		return cpuTime;
	}

	/**
	 * Getter for the user time associated with the Node run.
	 * 
	 * @return The user time associated with the Node run.
	 */
	public long getUserTime() {
		return userTime;
	}
}
