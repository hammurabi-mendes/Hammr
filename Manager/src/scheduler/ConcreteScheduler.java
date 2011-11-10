/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package scheduler;

import java.rmi.RemoteException;

import java.util.Set;
import java.util.Map;
import java.util.Queue;

import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;

import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import execinfo.NodeGroup;
import execinfo.NodeGroupBundle;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import appspecs.ApplicationSpecification;
import appspecs.Node;
import appspecs.Edge;
import appspecs.EdgeType;

import utilities.MutableInteger;

import interfaces.Launcher;

import manager.ConcreteManager;

public class ConcreteScheduler implements Scheduler {
	private ConcreteManager concreteManager;
	private ApplicationSpecification applicationSpecification;

	// A NodeGroupBundle is only released when its NodeGroup dependencies are executed
	
	private DependencyManager<NodeGroup, NodeGroupBundle> dependencyManager;

	// List of NodeGroups currently executin on Launchers
	
	private Map<Long, NodeGroup> scheduledNodeGroups;

	private long serialNumberCounter = 1L;

	/**
	 * Constructor method.
	 * 
	 * @param concreteManager Reference to the manager object.
	 */
	public ConcreteScheduler(ConcreteManager concreteManager) {
		this.concreteManager = concreteManager;
	}

	/**
	 * Specifies the application that this scheduler is responsible by.
	 * 
	 * @param applicationSpecification The application this scheduler is responsible by.
	 */
	private void setApplicationSpecification(ApplicationSpecification applicationSpecification) {
		this.applicationSpecification = applicationSpecification;
	}

	/**
	 * Parses the graph and clusters Nodes that use shared memory as their communication primitive
	 * into NodeGroups. Each NodeGroup is assigned a serial number.
	 * 
	 * @return A list of NodeGroups indexed by their serial number.
	 */
	private Map<MutableInteger, NodeGroup> getNodeGroups() {
		Map<MutableInteger, NodeGroup> result = new HashMap<MutableInteger, NodeGroup>();

		Queue<Node> queue = new LinkedList<Node>();

		int currentSpammerIdentifier = 0;

		Node current, neighbor;

		for(Node spammer: applicationSpecification.vertexSet()) {
			if(spammer.isMarked()) {
				continue;
			}

			Set<Node> spammerGroup = new HashSet<Node>();

			MutableInteger spammerIdentifier = new MutableInteger(currentSpammerIdentifier++);

			spammer.setMark(spammerIdentifier);
			queue.add(spammer);

			while(queue.size() > 0) {
				current = queue.remove();

				spammerGroup.add(current);

				for(Edge connection: applicationSpecification.outgoingEdgesOf(current)) {
					neighbor = connection.getTarget();

					if(connection.getCommunicationMode() == EdgeType.SHM) {
						if(!neighbor.isMarked()) {
							neighbor.setMark(spammerIdentifier);
							queue.add(neighbor);
						}
					}
				}

				for(Edge connection: applicationSpecification.incomingEdgesOf(current)) {
					neighbor = connection.getSource();

					if(connection.getCommunicationMode() == EdgeType.SHM) {
						if(!neighbor.isMarked()) {
							neighbor.setMark(spammerIdentifier);
							queue.add(neighbor);
						}
					}
				}
			}

			result.put(spammerIdentifier, new NodeGroup(applicationSpecification.getName(), spammerGroup));
		}

		return result;
	}

	/**
	 * Parses the graph formed when we consider NodeGroups as single nodes and cluster NodeGroups that use TCP channels
	 * as their communication primitive into NodeGroupBundles. Each NodeGroupBundle is assigned a serial number.
	 * 
	 * @return A list of NodeGroups indexed by their serial number.
	 */
	private Map<MutableInteger, NodeGroupBundle> getNodeGroupBundles() {
		Map<MutableInteger, NodeGroup> nodeGroups = getNodeGroups();

		DefaultDirectedGraph<NodeGroup, DefaultEdge> nodeGroupGraph = new DefaultDirectedGraph<NodeGroup, DefaultEdge>(DefaultEdge.class);

		for(NodeGroup nodeGroup: nodeGroups.values()) {
			nodeGroupGraph.addVertex(nodeGroup);
		}

		Node source, target;

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == EdgeType.TCP) {
				source = edge.getSource();
				target = edge.getTarget();

				nodeGroupGraph.addEdge(source.getNodeGroup(), target.getNodeGroup());
			}
		}

		Map<MutableInteger, NodeGroupBundle> result = new HashMap<MutableInteger, NodeGroupBundle>();

		Queue<NodeGroup> queue = new LinkedList<NodeGroup>();

		int currentBundleIdentifier = 0;

		NodeGroup current, neighbor;

		for(NodeGroup spammer: nodeGroupGraph.vertexSet()) {
			if(spammer.isMarked()) {
				continue;
			}

			Set<NodeGroup> spammerBundle = new HashSet<NodeGroup>();

			MutableInteger spammerIdentifier = new MutableInteger(currentBundleIdentifier++);

			spammer.setMark(spammerIdentifier);
			queue.add(spammer);

			while(queue.size() > 0) {
				current = queue.remove();

				spammerBundle.add(current);

				for(DefaultEdge connection: nodeGroupGraph.outgoingEdgesOf(current)) {
					neighbor = nodeGroupGraph.getEdgeTarget(connection);

					if(!neighbor.isMarked()) {
						neighbor.setMark(spammerIdentifier);
						queue.add(neighbor);
					}
				}

				for(DefaultEdge connection: nodeGroupGraph.incomingEdgesOf(current)) {
					neighbor = nodeGroupGraph.getEdgeSource(connection);

					if(!neighbor.isMarked()) {
						neighbor.setMark(spammerIdentifier);
						queue.add(neighbor);
					}
				}
			}

			result.put(spammerIdentifier, new NodeGroupBundle(spammerBundle));
		}

		return result;
	}

	// TODO: Insert the following functionality
	//       1) Verify whether all the initial node bundles are free (i.e., without dependencies)
	//       2) Add all the free dependencies into the dependency manager
	//       3) Guarantee that one file is read at most by one node
	/**
	 * Based on the NodeGroupBundles identified in the application specification, create dependencies that only release
	 * NodeGroupBundles when all their triggerer NodeGroups have their execution notified to the scheduler.
	 * @throws TemporalDependencyException
	 * @throws CyclicDependencyException
	 */
	private void createNodeGroupBundleDependencies() throws TemporalDependencyException, CyclicDependencyException {
		Map<MutableInteger, NodeGroupBundle> nodeGroupBundles = getNodeGroupBundles();

		System.out.println("Identified node group bundles:");

		for(NodeGroupBundle x: nodeGroupBundles.values()) {
			System.out.println(x);
		}

		DefaultDirectedGraph<NodeGroupBundle, DefaultEdge> nodeGroupBundleGraph = new DefaultDirectedGraph<NodeGroupBundle, DefaultEdge>(DefaultEdge.class);

		for(NodeGroupBundle nodeGroupBundle: nodeGroupBundles.values()) {
			nodeGroupBundleGraph.addVertex(nodeGroupBundle);
		}

		Node source, target;

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == EdgeType.FILE) {
				source = edge.getSource();
				target = edge.getTarget();

				nodeGroupBundleGraph.addEdge(source.getNodeGroup().getNodeGroupBundle(), target.getNodeGroup().getNodeGroupBundle());
			}
		}

		CycleDetector<NodeGroupBundle, DefaultEdge> cycleDetector = new CycleDetector<NodeGroupBundle, DefaultEdge>(nodeGroupBundleGraph);

		if(cycleDetector.detectCycles()) {
			throw new CyclicDependencyException();
		}

		for(Node node: applicationSpecification.getInitials()) {
			dependencyManager.insertDependency(null, node.getNodeGroup().getNodeGroupBundle());
		}

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == EdgeType.FILE) {
				source = edge.getSource();
				target = edge.getTarget();

				if(source.getNodeGroup().getNodeGroupBundle() == target.getNodeGroup().getNodeGroupBundle()) {
					throw new TemporalDependencyException(source, target);
				}

				dependencyManager.insertDependency(source.getNodeGroup(), target.getNodeGroup().getNodeGroupBundle());
			}
		}
	}

	/**
	 * Try to schedule the next wave of NodeGroups: NodeGroupBundles are NodeGroups that should
	 * be schedule at the same time.
	 * 
	 * @return False if no NodeGroupBundle is available to execution; true otherwise.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the next wave of NodeGroups.
	 */
	public synchronized boolean scheduleNodeGroupBundle() throws InsufficientLaunchersException {
		if(!dependencyManager.hasFreeDependents()) {
			return false;
		}

		Set<NodeGroupBundle> freeNodeGroupBundles = dependencyManager.obtainFreeDependents();

		for(NodeGroupBundle freeNodeGroupBundle: freeNodeGroupBundles) {
			System.out.println("Scheduling node bundle " + freeNodeGroupBundle);

			for(NodeGroup nodeGroup: freeNodeGroupBundle) {
				scheduleNodeGroup(nodeGroup);
			}
		}

		return true;
	}

	/**
	 * Try to schedule the informed NodeGroup.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the next wave of NodeGroups.
	 */
	private void scheduleNodeGroup(NodeGroup nodeGroup) throws InsufficientLaunchersException {
		nodeGroup.prepareSchedule(serialNumberCounter++);

		while(true) {
			try {
				Launcher launcher = getRandomLauncher();

				scheduledNodeGroups.put(nodeGroup.getSerialNumber(), nodeGroup);

				launcher.addNodeGroup(nodeGroup);

				break;
			} catch (RemoteException exception) {
				System.err.println("Failed using launcher, trying next one...");

				scheduledNodeGroups.remove(nodeGroup.getSerialNumber());

				exception.printStackTrace();
			}
		}	
	}

	/**
	 * Obtains a random alive Launcher from the Manager.
	 * 
	 * @return A random alive Launcher.
	 * 
	 * @throws InsufficientLaunchersException If there are no alive Launchers.
	 */
	private Launcher getRandomLauncher() throws InsufficientLaunchersException {
		Launcher launcher =  concreteManager.getRandomLauncher();

		if(launcher == null) {
			throw new InsufficientLaunchersException();
		}

		return launcher;
	}

	/**
	 * Informs the scheduler a particular NodeGroup has finished its execution.
	 * 
	 * @param serialNumber The serial number of the NodeGroup that has finished its execution.
	 * 
	 * @return True if this is the first termination notification for this NodeGroup; false otherwise. The current
	 * scheduler implementation only has one possible termination notification, since it doesn't handle failures.
	 */
	public synchronized boolean handleTermination(Long serialNumber) {
		NodeGroup terminated = scheduledNodeGroups.remove(serialNumber);

		if(terminated != null) {
			dependencyManager.removeDependency(terminated);

			return true;
		}

		return false;
	}

	/**
	 * Setups the scheduler for the new application being executed.
	 * @param applicationSpecification Application specification.
	 * 
	 * @return True if the setup finished successfully; false otherwise.
	 * 
	 * @throws TemporalDependencyException If the application specification has a temporal dependency problem.
	 * @throws CyclicDependencyException If the application specification has a cyclic dependency problem.
	 */
	public synchronized boolean setup(ApplicationSpecification applicationSpecification) throws TemporalDependencyException, CyclicDependencyException {
		setApplicationSpecification(applicationSpecification);

		dependencyManager = new DependencyManager<NodeGroup, NodeGroupBundle>();

		scheduledNodeGroups = new HashMap<Long, NodeGroup>();

		long graphParsingStartTimer = System.currentTimeMillis();

		createNodeGroupBundleDependencies();

		long graphParsingEndingTimer = System.currentTimeMillis();

		System.out.println("Time to parse graph for application " + applicationSpecification.getName() + ": " + (graphParsingEndingTimer - graphParsingStartTimer) + " msec");

		return true;
	}

	/**
	 * Tests whether all the Node/NodeGroups were already executed.
	 * 
	 * @return True if all the Node/NodeGroups were already executed, false otherwise.
	 */
	public synchronized boolean finished() {
		return (!dependencyManager.hasLockedDependents() && !dependencyManager.hasFreeDependents() && (scheduledNodeGroups.size() == 0));
	}
}
