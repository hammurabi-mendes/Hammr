package execinfo;

import java.io.Serializable;

import java.util.Collection;

import java.util.Set;
import java.util.HashSet;

import appspecs.Node;

import utilities.MutableInteger;

public class NodeGroup extends HashSet<Node> implements Serializable {
	private static final long serialVersionUID = 1L;

	private String application;
	private long serialNumber;

	private MutableInteger mark;

	private NodeGroupBundle nodeGroupBundle;

	public NodeGroup(String application, Node node) {
		super();

		setApplication(application);
		addNode(node);
	}

	public NodeGroup(String application, Set<Node> nodes) {
		super();

		setApplication(application);
		addNodes(nodes);
	}

	public boolean add() {
		assert false;

		return false;
	}

	private boolean addNode(Node node) {
		if(node.getNodeGroup() != null) {
			assert false;

			return false;
		}

		node.setNodeGroup(this);

		super.add(node);

		return true;
	}

	private boolean addNodes(Collection<Node> nodes) {
		for(Node node: nodes) {
			if(node.getNodeGroup() != null) {
				assert false;

				return false;
			}
		}

		for(Node node: nodes) {
			node.setNodeGroup(this);

			super.add(node);
		}

		return true;
	}

	public Set<Node> getNodes() {
		return this;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApplication() {
		return application;
	}

	public void setSerialNumber(long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public long getSerialNumber() {
		return serialNumber;
	}

	public MutableInteger getMark() {
		return mark;
	}

	public void setMark(MutableInteger mark) {
		if(!isMarked() || mark == null) {
			this.mark = mark;
		}
		else {
			this.mark.setValue(mark.getValue());
		}
	}

	public boolean isMarked() {
		return (mark != null);
	}

	public void setNodeGroupBundle(NodeGroupBundle nodeGroupBundle) {
		this.nodeGroupBundle = nodeGroupBundle;
	}

	public NodeGroupBundle getNodeGroupBundle() {
		return nodeGroupBundle;
	}

	public void prepareSchedule(long serialNumber) {
		setSerialNumber(serialNumber);

		//setNodeGroupBundle(null); ? why doing this?
	}

	public String toString() {
		String result = "[";

		boolean firstNode = true;

		for(Node node: getNodes()) {
			if(!firstNode) {
				result += ", ";
			}

			firstNode = false;

			result += node;
		}

		result += "]";

		return result;
	}
	
	/**
	 * Get the id of the node group. The id of node group is dynamic and would
	 * change if it runs several times.
	 * 
	 * @return
	 */
	public final String getId() {
		return "NodeGroup-" + application + "-" + serialNumber;
	}
}
