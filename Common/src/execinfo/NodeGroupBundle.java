package execinfo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import execinfo.NodeGroup;

public class NodeGroupBundle extends HashSet<NodeGroup> implements Serializable {
	private static final long serialVersionUID = 1L;

	public NodeGroupBundle() {
		super();
	}

	public NodeGroupBundle(NodeGroup nodeGroup) {
		this();

		addNodeGroup(nodeGroup);
	}

	public NodeGroupBundle(Set<NodeGroup> nodeGroups) {
		this();

		addNodeGroups(nodeGroups);
	}	

	public boolean add() {
		assert false;

		return false;
	}

	public boolean addNodeGroup(NodeGroup nodeGroup) {
		if(nodeGroup.getNodeGroupBundle() != null) {
			assert false;

			return false;
		}

		nodeGroup.setNodeGroupBundle(this);

		super.add(nodeGroup);

		return true;
	}

	public boolean addNodeGroups(Set<NodeGroup> nodeGroups) {
		for(NodeGroup nodeGroup: nodeGroups) {
			if(nodeGroup.getNodeGroupBundle() != null) {
				assert false;

				return false;
			}
		}

		for(NodeGroup nodeGroup: nodeGroups) {
			nodeGroup.setNodeGroupBundle(this);

			super.add(nodeGroup);
		}	

		return true;
	}

	public String toString() {
		String result = "{\n";

		for(NodeGroup nodeGroup: this) {
			result += "\t";
			result += nodeGroup;
			result += "\n";
		}

		result += "}";

		return result;
	}
}
