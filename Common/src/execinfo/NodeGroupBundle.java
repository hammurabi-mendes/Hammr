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
