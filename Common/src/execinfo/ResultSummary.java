/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package execinfo;

import java.util.Set;
import java.util.HashSet;

import java.util.Map;
import java.util.HashMap;

import java.io.Serializable;

import execinfo.aggregators.Aggregator;

public class ResultSummary implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nodeGroupApplication;
	private long nodeGroupSerialNumber;

	private long nodeGroupTiming;

	// Maps nodes to individual measurements
	private Map<String, NodeMeasurements> measurements;

	// Aggregators for the nodes
	private Set<Aggregator<? extends Object>> aggregators;

	private Type type;

	public ResultSummary(String nodeGroupApplication, long nodeGroupSerialNumber, Type type) {
		setNodeGroupApplication(nodeGroupApplication);
		setNodeGroupSerialNumber(nodeGroupSerialNumber);

		if(type == Type.SUCCESS) {
			measurements = new HashMap<String, NodeMeasurements>();

			aggregators = new HashSet<Aggregator<? extends Object>>();
		}

		setType(type);
	}

	public void setNodeGroupApplication(String nodeGroupApplication) {
		this.nodeGroupApplication = nodeGroupApplication;
	}

	public String getNodeGroupApplication() {
		return nodeGroupApplication;
	}

	public void setNodeGroupSerialNumber(long nodeGroupIdentifier) {
		this.nodeGroupSerialNumber = nodeGroupIdentifier;
	}

	public long getNodeGroupSerialNumber() {
		return nodeGroupSerialNumber;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public long getNodeGroupTiming() {
		return nodeGroupTiming;
	}

	public void setNodeGroupTiming(long nodeGroupTiming) {
		this.nodeGroupTiming = nodeGroupTiming;
	}

	public Set<String> getNodeNames() {
		return measurements.keySet();
	}

	public NodeMeasurements getNodeMeasurement(String nodeName) {
		return measurements.get(nodeName);
	}

	public void addNodeMeasurements(String nodeName, NodeMeasurements nodeMeasurements) {
		measurements.put(nodeName, nodeMeasurements);
	}

	public Set<Aggregator<? extends Object>> getAggregators() {
		return aggregators;
	}

	public void addAggregator(Aggregator<? extends Object> aggregator) {
		aggregators.add(aggregator);
	}

	public enum Type {
		SUCCESS, FAILURE;
	}
}
