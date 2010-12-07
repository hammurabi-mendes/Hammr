package manager;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import scheduler.Scheduler;

import execinfo.ResultSummary;

public class ApplicationInformationHolder {
	private String applicationName;

	private ApplicationSpecification applicationSpecification;

	private Scheduler applicationScheduler;

	private Map<String, InetSocketAddress> registeredSocketAddresses;

	private Set<ResultSummary> receivedResultSummaries;
	
	private long globalTimerStart = -1L;
	private long globalTimerFinish = -1L;

	public ApplicationInformationHolder() {
		this.registeredSocketAddresses = new HashMap<String, InetSocketAddress>();

		this.receivedResultSummaries = new HashSet<ResultSummary>();
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public ApplicationSpecification getApplicationSpecification() {
		return applicationSpecification;
	}

	public void setApplicationSpecification(ApplicationSpecification applicationSpecification) {
		this.applicationSpecification = applicationSpecification;
	}

	public Scheduler getApplicationScheduler() {
		return applicationScheduler;
	}

	public void setApplicationScheduler(Scheduler applicationScheduler) {
		this.applicationScheduler = applicationScheduler;
	}

	public void addRegisteredSocketAddresses(String identifier, InetSocketAddress registeredSocketAddress) {
		registeredSocketAddresses.put(identifier, registeredSocketAddress);
	}

	public InetSocketAddress getRegisteredSocketAddress(String identifier) {
		return registeredSocketAddresses.get(identifier);
	}

	public void addReceivedResultSummaries(ResultSummary resultSummary) {
		this.receivedResultSummaries.add(resultSummary);
	}

	public Set<ResultSummary> getReceivedResultSummaries() {
		return receivedResultSummaries;
	}
	
	public void markStart() {
		globalTimerStart = System.currentTimeMillis();
	}
	
	public void markFinish() {
		globalTimerFinish = System.currentTimeMillis();
	}
	
	public long getTotalRunningTime() {
		return globalTimerFinish - globalTimerStart;
	}
}
