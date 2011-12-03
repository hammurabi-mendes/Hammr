/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package execinfo;

import interfaces.Manager;
import interfaces.Launcher;

import java.util.Collection;

import java.io.Serializable;

import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

import appspecs.Node;

import utilities.MutableInteger;

public class NodeGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	/////////////////////////
	// RUNNING INFORMATION //
	/////////////////////////

	private String application;

	private long serialNumber;

	private Set<Node> set;

	private Launcher currentLauncher;
	private Launcher previousLauncher;

	private Manager manager;

	/////////////////////////
	// PARSING INFORMATION //
	/////////////////////////

	private NodeGroupBundle nodeGroupBundle;

	private MutableInteger mark;

	public NodeGroup(String application, Node node) {
		set = new HashSet<Node>();

		setApplication(application);
		addNode(node);
	}

	public NodeGroup(String application, Set<Node> nodes) {
		set = new HashSet<Node>();

		setApplication(application);
		addNodes(nodes);
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

	private boolean addNode(Node node) {
		if(node.getNodeGroup() != null) {
			assert false;

			return false;
		}

		node.setNodeGroup(this);

		set.add(node);

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

			set.add(node);
		}

		return true;
	}

	public Set<Node> getNodes() {
		return set;
	}

	public Iterator<Node> getNodesIterator() {
		return set.iterator();
	}

	public int getSize() {
		return set.size();
	}

	public Launcher getCurrentLauncher() {
		return currentLauncher;
	}

	public void setCurrentLauncher(Launcher currentLauncher) {
		this.currentLauncher = currentLauncher;
	}

	public Launcher getPreviousLauncher() {
		return previousLauncher;
	}

	public void setPreviousLauncher(Launcher previousLauncher) {
		this.previousLauncher = previousLauncher;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public void setNodeGroupBundle(NodeGroupBundle nodeGroupBundle) {
		this.nodeGroupBundle = nodeGroupBundle;
	}

	public NodeGroupBundle getNodeGroupBundle() {
		return nodeGroupBundle;
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

	public void prepareSchedule(long serialNumber) {
		setSerialNumber(serialNumber);

		setNodeGroupBundle(null);

		setMark(null);
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
}
