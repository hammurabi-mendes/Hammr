package execinfo;

import java.util.Set;

import java.util.Map;
import java.util.HashMap;

import java.io.Serializable;

public class ResultSummary implements Serializable {
	private static final long serialVersionUID = 1L;

	private String nodeGroupApplication;
	private long nodeGroupSerialNumber;

	private long nodeGroupTiming;

	private Map<String, NodeMeasurements> nodeTimings;

	private Type type;

	public ResultSummary(String nodeGroupApplication, long nodeGroupSerialNumber, Type type) {
		setNodeGroupApplication(nodeGroupApplication);
		setNodeGroupSerialNumber(nodeGroupSerialNumber);

		if(type == Type.SUCCESS) {
			nodeTimings = new HashMap<String, NodeMeasurements>();
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
		return nodeTimings.keySet();
	}

	public NodeMeasurements getNodeMeasurement(String nodeName) {
		return nodeTimings.get(nodeName);
	}

	public void addNodeMeasurements(String nodeName, NodeMeasurements nodeMeasurements) {
		nodeTimings.put(nodeName, nodeMeasurements);
	}

	public enum Type {
		SUCCESS, FAILURE;
	}
}
