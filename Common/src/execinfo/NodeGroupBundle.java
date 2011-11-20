/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package execinfo;

import java.io.Serializable;

import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

import execinfo.NodeGroup;

public class NodeGroupBundle implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<NodeGroup> nodeGroups;

	public NodeGroupBundle() {
		nodeGroups = new HashSet<NodeGroup>();
	}

	public NodeGroupBundle(NodeGroup nodeGroup) {
		nodeGroups = new HashSet<NodeGroup>();

		addNodeGroup(nodeGroup);
	}

	public NodeGroupBundle(Set<NodeGroup> nodeGroups) {
		nodeGroups = new HashSet<NodeGroup>();

		addNodeGroups(nodeGroups);
	}	

	public boolean addNodeGroup(NodeGroup nodeGroup) {
		if(nodeGroup.getNodeGroupBundle() != null) {
			assert false;

			return false;
		}

		nodeGroup.setNodeGroupBundle(this);

		nodeGroups.add(nodeGroup);

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

			nodeGroups.add(nodeGroup);
		}	

		return true;
	}

	public Set<NodeGroup> getNodeGroups() {
		return nodeGroups;
	}

	public Iterator<NodeGroup> getNodeGroupsIterator() {
		return nodeGroups.iterator();
	}

	public int getSize() {
		return nodeGroups.size();
	}

	public String toString() {
		String result = "{\n";

		for(NodeGroup nodeGroup: nodeGroups) {
			result += "\t";
			result += nodeGroup;
			result += "\n";
		}

		result += "}";

		return result;
	}
}
