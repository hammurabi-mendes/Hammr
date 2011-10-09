package manager;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import scheduler.Scheduler;

import execinfo.ResultSummary;

/**
 * Packs information regarding an executing application in the manager. The information contained is
 * the application name, specification, scheduler, the current registered server-side TCP channels and
 * the result summaries for the finalized NodeGroups. 
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ApplicationInformationHolder {
	private String applicationName;

	private ApplicationSpecification applicationSpecification;

	private Scheduler applicationScheduler;

	private Map<String, InetSocketAddress> registeredSocketAddresses;

	private Set<ResultSummary> receivedResultSummaries;
	
	private long globalTimerStart = -1L;
	private long globalTimerFinish = -1L;

	/**
	 * Class constructor.
	 */
	public ApplicationInformationHolder() {
		this.registeredSocketAddresses = new HashMap<String, InetSocketAddress>();

		this.receivedResultSummaries = new HashSet<ResultSummary>();
	}

	/**
	 * Getter for the application name.
	 * 
	 * @return The application name.
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Setter for the application name.
	 * 
	 * @param applicationName The new application name.
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Getter for the application specification.
	 * 
	 * @return The application specification.
	 */
	public ApplicationSpecification getApplicationSpecification() {
		return applicationSpecification;
	}

	/**
	 * Setter for the application specification.
	 * 
	 * @param applicationName The new application specification.
	 */
	public void setApplicationSpecification(ApplicationSpecification applicationSpecification) {
		this.applicationSpecification = applicationSpecification;
	}

	/**
	 * Getter for the application scheduler.
	 * 
	 * @return The application scheduler.
	 */
	public Scheduler getApplicationScheduler() {
		return applicationScheduler;
	}

	/**
	 * Setter for the application scheduler.
	 * 
	 * @param applicationName The new application scheduler.
	 */
	public void setApplicationScheduler(Scheduler applicationScheduler) {
		this.applicationScheduler = applicationScheduler;
	}

	/**
	 * Insert a socket address for a server-side TCP channel.
	 * 
	 * @param identifier Node that has a server-side TCP channel.
	 * @param registeredSocketAddress Socket address associated with the server-side TCP channel.
	 */
	public void addRegisteredSocketAddresses(String identifier, InetSocketAddress registeredSocketAddress) {
		registeredSocketAddresses.put(identifier, registeredSocketAddress);
	}

	/**
	 * Obtains the associated socket address for a particular Node's server-side TCP channel.
	 * 
	 * @param identifier Node name.
	 * 
	 * @return The socket address associated with the server-side TCP channel for the specified Node.
	 */
	public InetSocketAddress getRegisteredSocketAddress(String identifier) {
		return registeredSocketAddresses.get(identifier);
	}

	/**
	 * Inserts a received NodeGroup runtime information into the holder.
	 * 
	 * @param resultSummary The received runtime information.
	 */
	public void addReceivedResultSummaries(ResultSummary resultSummary) {
		this.receivedResultSummaries.add(resultSummary);
	}

	/**
	 * Obtain the previously received NodeGroup runtime information summaries.
	 * 
	 * @return The previously received NodeGroup runtime information summaries.
	 */
	public Set<ResultSummary> getReceivedResultSummaries() {
		return receivedResultSummaries;
	}
	
	/**
	 * Obtain and registers the current real start time of the application.
	 */
	public void markStart() {
		globalTimerStart = System.currentTimeMillis();
	}
	
	/**
	 * Obtain and registers the current real finish time of the application.
	 */
	public void markFinish() {
		globalTimerFinish = System.currentTimeMillis();
	}
	
	/**
	 * Obtain the total running (real) time of the application.
	 * 
	 * @return The total running (real) time of the application.
	 */
	public long getTotalRunningTime() {
		return globalTimerFinish - globalTimerStart;
	}
}
