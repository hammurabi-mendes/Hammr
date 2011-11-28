/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package manager;

import java.util.Set;
import java.util.HashSet;

import java.util.Map;
import java.util.HashMap;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import scheduler.Scheduler;

import execinfo.ResultSummary;

import execinfo.aggregators.Aggregator;

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

	private Map<String, Aggregator<? extends Object>> aggregators;

	private long globalTimerStart = -1L;
	private long globalTimerFinish = -1L;

	/**
	 * Class constructor.
	 */
	public ApplicationInformationHolder() {
		this.registeredSocketAddresses = new HashMap<String, InetSocketAddress>();

		this.receivedResultSummaries = new HashSet<ResultSummary>();

		this.aggregators = new HashMap<String, Aggregator<? extends Object>>();
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
	 * Inserts a received NodeGroup runtime information into the holder, and updates the aggregators
	 * corresponding to the application.
	 * 
	 * @param resultSummary The received runtime information.
	 */
	public void addReceivedResultSummaries(ResultSummary resultSummary) {
		this.receivedResultSummaries.add(resultSummary);

		for(Aggregator<? extends Object> currentAggregator: resultSummary.getAggregators()) {
			Aggregator<? extends Object> previousAggregator = aggregators.get(currentAggregator.getVariable());

			if(previousAggregator == null) {
				aggregators.put(currentAggregator.getVariable(), currentAggregator);
			}
			else {
				previousAggregator.mergeAggregators(currentAggregator);
			}
		}
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

	/**
	 * Returns the aggregator associated with the indicated variable.
	 * 
	 * @param variable The variable that indexes the aggregator.
	 * 
	 * @return The aggregator associated with the indicated variable.
	 */
	public Aggregator<? extends Object> getAggregator(String variable) {
		return aggregators.get(variable);
	}

	/**
	 * Returns the mapping for all aggregators for this application.
	 * 
	 * @return The mapping for all the aggregators for this application.
	 */
	public Map<String, Aggregator<? extends Object>> getAggregators() {
		return aggregators;
	}
}
