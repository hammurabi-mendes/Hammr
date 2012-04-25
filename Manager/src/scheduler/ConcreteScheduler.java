/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package scheduler;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Collections;

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Queue;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;

import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import enums.CommunicationMode;

import execinfo.NodeGroup;
import execinfo.NodeGroupBundle;

import exceptions.InexistentInputException;
import exceptions.InexistentOutputException;

import exceptions.ParsingNodeGroupPlacementException;

import exceptions.RuntimeGlobalPlacementException;
import exceptions.RuntimeNodeGroupPlacementException;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import appspecs.ApplicationSpecification;
import appspecs.Decider;
import appspecs.Node;
import appspecs.Edge;

import security.restrictions.LauncherRestrictions;

import utilities.MutableInteger;
import utilities.RMIHelper;

import utilities.filesystem.FileHelper;
import utilities.filesystem.Filename;

import interfaces.ApplicationAggregator;
import interfaces.ApplicationController;

import interfaces.Launcher;

import manager.ConcreteManager;

public class ConcreteScheduler implements Scheduler {
	private String applicationName;

	private ApplicationSpecification applicationSpecification;

	/////////////////////////
	// PARSING INFORMATION //
	/////////////////////////

	Map<MutableInteger, NodeGroup> nodeGroups;
	Map<MutableInteger, NodeGroupBundle> nodeGroupBundles;

	// A NodeGroupBundle is only released when its NodeGroup dependencies are executed

	private DependencyManager<NodeGroup, NodeGroupBundle> dependencyManager;

	// List of NodeGroups prepared, running, and terminated

	private Map<Long, NodeGroup> runningNodeGroups;

	private long serialNumberCounter = 1L;

	private Map<NodeGroup, NodeGroupBundle> convertGroupToBundle;

	/**
	 * Constructor method.
	 * 
	 * @param applicationName The name of the application this scheduler works on.
	 */
	public ConcreteScheduler(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Setups the scheduler for the new application being executed.
	 * 
	 * @throws TemporalDependencyException If the application specification has a temporal dependency problem.
	 * @throws CyclicDependencyException If the application specification has a cyclic dependency problem.
	 *                                   A cyclic is only a problem when it involves files (TCP/SHM cycles are allowed).
	 * @throws ParsingNodeGroupPlacementException If none of the (existing) alive launchers would ever meet the restrictions imposed by all
	 *         members of the NodeGroup parsed.
	 */
	public synchronized void prepareApplication() throws TemporalDependencyException, CyclicDependencyException, ParsingNodeGroupPlacementException {
		// Initiate data structures

		this.applicationSpecification = ConcreteManager.getInstance().getApplicationInformationHolder(applicationName).getApplicationSpecification();

		this.dependencyManager = new DependencyManager<NodeGroup, NodeGroupBundle>();

		this.runningNodeGroups = new HashMap<Long, NodeGroup>();

		this.convertGroupToBundle = new HashMap<NodeGroup, NodeGroupBundle>();

		// Parse the application graph

		long graphParsingStartTimer = System.currentTimeMillis();

		Map<MutableInteger, NodeGroupBundle> nodeGroupBundles = getNodeGroupBundles();

		// Map the NodeGroups to the NodeGroupBundles

		for(NodeGroupBundle nodeGroupBundle: nodeGroupBundles.values()) {
			for(NodeGroup nodeGroup: nodeGroupBundle.getNodeGroups()) {
				convertGroupToBundle.put(nodeGroup, nodeGroupBundle);
			}
		}

		long graphParsingEndingTimer = System.currentTimeMillis();

		System.out.println("Time to parse graph for application " + applicationSpecification.getName() + ": " + (graphParsingEndingTimer - graphParsingStartTimer) + " msec");

		// Display identified node group bundles

		System.out.println("Identified node group bundles:");

		for(NodeGroupBundle x: nodeGroupBundles.values()) {
			System.out.println(x);
		}

		// Detect cyclic dependency problems

		Node source, target;

		DefaultDirectedGraph<NodeGroupBundle, DefaultEdge> nodeGroupBundleGraph = new DefaultDirectedGraph<NodeGroupBundle, DefaultEdge>(DefaultEdge.class);

		for(NodeGroupBundle nodeGroupBundle: nodeGroupBundles.values()) {
			nodeGroupBundleGraph.addVertex(nodeGroupBundle);
		}

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == CommunicationMode.FILE) {
				source = edge.getSource();
				target = edge.getTarget();

				nodeGroupBundleGraph.addEdge(convertGroupToBundle.get(source.getNodeGroup()), convertGroupToBundle.get(target.getNodeGroup()));
			}
		}

		CycleDetector<NodeGroupBundle, DefaultEdge> cycleDetector = new CycleDetector<NodeGroupBundle, DefaultEdge>(nodeGroupBundleGraph);

		if(cycleDetector.detectCycles()) {
			throw new CyclicDependencyException();
		}

		// Detect temporal dependency problems

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == CommunicationMode.FILE) {
				source = edge.getSource();
				target = edge.getTarget();

				if(convertGroupToBundle.get(source.getNodeGroup()) == convertGroupToBundle.get(target.getNodeGroup())) {
					throw new TemporalDependencyException(source, target);
				}
			}
		}

		// Detect node placement problems: two nodes in the same NodeGroup with different placement restrictions
		for(NodeGroup nodeGroup: nodeGroups.values()) {
			Set<String> intersection = null;

			for(Node node: nodeGroup.getNodes()) {
				LauncherRestrictions nodeRestriction = applicationSpecification.obtainNodeRestriction(node);

				if(nodeRestriction != null && nodeRestriction.getLauncherIds().size() > 0) {
					if(intersection == null) {
						intersection = new HashSet<String>(nodeRestriction.getLauncherIds());
					}
					else {
						intersection.retainAll(nodeRestriction.getLauncherIds());
					}
				}
			}

			if(intersection != null && intersection.size() == 0) {
				throw new ParsingNodeGroupPlacementException();
			}
		}

		// Exports all the aggregators

		for(String variable: applicationSpecification.getAggregators().keySet()) {
			ApplicationAggregator<? extends Serializable,? extends Serializable> aggregator = applicationSpecification.getAggregator(variable);

			RMIHelper.exportRemoteObject(aggregator);
		}

		// Exports all the controllers

		for(String variable: applicationSpecification.getControllers().keySet()) {
			ApplicationController controller = applicationSpecification.getController(variable);

			RMIHelper.exportRemoteObject(controller);
		}

		// Sets each node group for execution in a separate JVM according to the application
		// and individual restrictions

		for(NodeGroup nodeGroup: nodeGroups.values()) {
			for(Node node: nodeGroup.getNodes()) {
				if(applicationSpecification.getGlobalRestrictions() != null && applicationSpecification.getGlobalRestrictions().isUseSeparateJVM()) {
					nodeGroup.setUseSeparateJVM(true);
					break;
				}

				if(applicationSpecification.obtainNodeRestriction(node) != null && applicationSpecification.obtainNodeRestriction(node).isUseSeparateJVM()) {
					nodeGroup.setUseSeparateJVM(true);
					break;
				}
			}
		}
	}

	/**
	 * Parses the graph and clusters Nodes that use shared memory as their communication primitive
	 * into NodeGroups. Each NodeGroup is assigned a serial number.
	 * 
	 * @return A list of NodeGroups indexed by their serial number.
	 */
	private Map<MutableInteger, NodeGroup> getNodeGroups() {
		if(nodeGroups != null) {
			return nodeGroups;
		}

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

					if(connection.getCommunicationMode() == CommunicationMode.SHM || connection.getCommunicationMode() == CommunicationMode.NODEBOX) {
						if(!neighbor.isMarked()) {
							neighbor.setMark(spammerIdentifier);
							queue.add(neighbor);
						}
					}
				}

				for(Edge connection: applicationSpecification.incomingEdgesOf(current)) {
					neighbor = connection.getSource();

					if(connection.getCommunicationMode() == CommunicationMode.SHM) {
						if(!neighbor.isMarked()) {
							neighbor.setMark(spammerIdentifier);
							queue.add(neighbor);
						}
					}
				}
			}

			result.put(spammerIdentifier, new NodeGroup(applicationSpecification.getName(), spammerGroup));
		}

		nodeGroups = result;
		return result;
	}

	/**
	 * Parses the graph formed when we consider NodeGroups as single nodes and cluster NodeGroups that use TCP channels
	 * as their communication primitive into NodeGroupBundles. Each NodeGroupBundle is assigned a serial number.
	 * 
	 * @return A list of NodeGroups indexed by their serial number.
	 */
	private Map<MutableInteger, NodeGroupBundle> getNodeGroupBundles() {
		if(nodeGroupBundles != null) {
			return nodeGroupBundles;
		}

		Map<MutableInteger, NodeGroup> nodeGroups = getNodeGroups();

		DefaultDirectedGraph<NodeGroup, DefaultEdge> nodeGroupGraph = new DefaultDirectedGraph<NodeGroup, DefaultEdge>(DefaultEdge.class);

		for(NodeGroup nodeGroup: nodeGroups.values()) {
			nodeGroupGraph.addVertex(nodeGroup);
		}

		Node source, target;

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == CommunicationMode.TCP || edge.getCommunicationMode() == CommunicationMode.TIMEBOX) {
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

		nodeGroupBundles = result;
		return result;
	}

	/**
	 * Terminates the application .
	 */
	public synchronized void terminateApplication() {
		// Do nothing special
	}

	/**
	 * Tests whether the application has finished.
	 * 
	 * @return True if the application has finished, false otherwise.
	 */
	public synchronized boolean finishedApplication() {
		Decider decider = applicationSpecification.getDecider();

		Map<String, ApplicationAggregator<? extends Serializable,? extends Serializable>> aggregators = applicationSpecification.getAggregators();

		if(decider == null) {
			return true;
		}

		// Run the graph transformation in the decider, and possibly
		// make new processes available to execute.

		decider.decideFollowingIteration(aggregators);

		// Returns true if the decider prepared a following iteration

		return !decider.requiresRunning();
	}

	/**
	 * Prepare an iteration for the application.
	 * 
	 * @throws InexistentInputException If one of the inputs are missing.
	 */
	public synchronized void prepareIteration() throws InexistentInputException {
		// Check if all the inputs are present

		List<Filename> missingInputs = new ArrayList<Filename>();;

		for(Filename input: applicationSpecification.getInputFilenames()) {
			if(!FileHelper.exists(input)) {
				missingInputs.add(input);
			}
		}

		if(missingInputs.size() != 0) {
			throw new InexistentInputException(missingInputs);
		}

		// Find out the initial nodes:
		// - If the application specification defines the intials, use them
		// - Otherwise get file consumers that only depend on system files

		Node source, target;

		Set<Node> initials = applicationSpecification.getInitials();

		if(initials.size() == 0) {
			initials = new HashSet<Node>(applicationSpecification.getFileConsumers());

			for(Edge edge: applicationSpecification.edgeSet()) {
				target = edge.getTarget();

				if(initials.contains(target)) {
					initials.remove(target);
				}
			}
		}

		// Notify the dependency manager that the initial nodes should be immediately available to schedule

		for(Node initial: initials) {
			dependencyManager.insertDependency(null, convertGroupToBundle.get(initial.getNodeGroup()));
		}

		// Notify the other dependencies for the dependency manager

		for(Edge edge: applicationSpecification.edgeSet()) {
			if(edge.getCommunicationMode() == CommunicationMode.FILE) {
				source = edge.getSource();
				target = edge.getTarget();

				dependencyManager.insertDependency(source.getNodeGroup(), convertGroupToBundle.get(target.getNodeGroup()));
			}
		}

		// Prepare all nodes and node groups for scheduling

		for(NodeGroup nodeGroup: nodeGroups.values()) {
			nodeGroup.prepareSchedule(serialNumberCounter++);

			for(Node node: nodeGroup.getNodes()) {
				node.prepareSchedule();
			}
		}
	}

	/**
	 * Terminates the iteration .
	 * 
	 * @throws InexistentOutputException If one of the outputs are missing.
	 */
	public synchronized void terminateIteration() throws InexistentOutputException {
		// If the iteration is finished, check if all the outputs are present

		List<Filename> missingOutputs = new ArrayList<Filename>();;

		for(Filename output: applicationSpecification.getOutputFilenames()) {
			if(!FileHelper.exists(output)) {
				missingOutputs.add(output);
			}
		}

		if(missingOutputs.size() != 0) {
			throw new InexistentOutputException(missingOutputs);
		}
	}

	/**
	 * Tests whether all the Node/NodeGroups were already executed for this iteration.
	 * 
	 * @return True if all the Node/NodeGroups were already executed for this iteration, false otherwise.
	 */
	public synchronized boolean finishedIteration() {
		return (!dependencyManager.hasLockedDependents() && !dependencyManager.hasUnlockedDependents() && (runningNodeGroups.size() == 0));
	}

	/**
	 * Try to schedule the next wave of NodeGroups: NodeGroupBundles are NodeGroups that should
	 * be schedule at the same time.
	 * 
	 * @return False if no NodeGroupBundle is available to execution; true otherwise.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the next wave of NodeGroups.
	 * @throws RuntimeGlobalPlacementException If none of the (existing) alive launchers meet the global restrictions imposed.
	 * @throws RuntimeNodeGroupPlacementException If none of the (existing) alive launchers meet the restrictions imposed by all
	 *         members of the NodeGroup being scheduled.
	 */
	public synchronized boolean schedule() throws InsufficientLaunchersException, RuntimeGlobalPlacementException, RuntimeNodeGroupPlacementException {
		if(!dependencyManager.hasUnlockedDependents()) {
			return false;
		}

		Set<NodeGroupBundle> freeNodeGroupBundles = dependencyManager.obtainFreeDependents();

		for(NodeGroupBundle freeNodeGroupBundle: freeNodeGroupBundles) {
			System.out.println("Scheduling node group bundle " + freeNodeGroupBundle);

			scheduleNodeGroupBundle(freeNodeGroupBundle);
		}

		return true;	
	}

	/**
	 * Try to schedule the informed NodeGroupBundle.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive NodeGroups.
	 * @throws RuntimeGlobalPlacementException If none of the (existing) alive launchers meet the global restrictions imposed.
	 * @throws RuntimeNodeGroupPlacementException If none of the (existing) alive launchers meet the restrictions imposed by all
	 *         members of the NodeGroup being scheduled.
	 */
	private void scheduleNodeGroupBundle(NodeGroupBundle nodeGroupBundle) throws InsufficientLaunchersException, RuntimeGlobalPlacementException, RuntimeNodeGroupPlacementException {
		for(NodeGroup nodeGroup: nodeGroupBundle.getNodeGroups()) {
			scheduleNodeGroup(nodeGroup);
		}
	}

	/**
	 * Try to schedule the informed NodeGroup.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the informed NodeGroup.
	 * 
	 * @throws RuntimeGlobalPlacementException If none of the (existing) alive launchers meet the global restrictions imposed.
	 * @throws RuntimeNodeGroupPlacementException If none of the (existing) alive launchers meet the restrictions imposed by all
	 *         members of the NodeGroup being scheduled.
	 */
	private void scheduleNodeGroup(NodeGroup nodeGroup) throws InsufficientLaunchersException, RuntimeGlobalPlacementException, RuntimeNodeGroupPlacementException {
		// Setup the previous/current launcher and manager references in the node group

		nodeGroup.setPreviousLauncher(nodeGroup.getCurrentLauncher());
		nodeGroup.setManager(ConcreteManager.getInstance());

		// First, try to reschedule the node group to the same launcher used before

		Launcher previousLauncher = nodeGroup.getPreviousLauncher();

		if(previousLauncher != null) {
			try {
				nodeGroup.setCurrentLauncher(previousLauncher);

				if(previousLauncher.addNodeGroup(nodeGroup)) {
					// Add node group to the running group
					runningNodeGroups.put(nodeGroup.getSerialNumber(), nodeGroup);

					return;
				}
			} catch (RemoteException exception) {
				System.err.println("Previous launcher for NodeGroup #" + nodeGroup.getSerialNumber() + " is no longer running. Trying a differnt one...");
			}
		}

		Collection<Launcher> collectionCurrentLaunchers = ConcreteManager.getInstance().getRegisteredLaunchers(applicationSpecification.getName(), nodeGroup.getNodes());

		List<Launcher> currentLaunchers = new ArrayList<Launcher>(collectionCurrentLaunchers);

		Collections.shuffle(currentLaunchers);

		for(int i = 0; i < currentLaunchers.size(); i++) {
			try {
				Launcher launcher = currentLaunchers.get(i);

				nodeGroup.setCurrentLauncher(launcher);

				if(launcher.addNodeGroup(nodeGroup)) {
					// Add node group to the running group
					runningNodeGroups.put(nodeGroup.getSerialNumber(), nodeGroup);

					return;
				}
				else {
					System.err.println("Failed using launcher (launcher unusable), trying next one...");
				}
			} catch (RemoteException exception) {
				System.err.println("Failed using launcher (launcher unreachable), trying next one...");
			}
		}	

		throw new InsufficientLaunchersException();
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
		NodeGroup terminated = runningNodeGroups.remove(serialNumber);

		if(terminated != null) {
			dependencyManager.removeDependency(terminated);

			return true;
		}

		return false;
	}
}