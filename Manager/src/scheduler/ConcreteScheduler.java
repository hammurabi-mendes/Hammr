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

	private DependencyManager<NodeGroup, NodeGroupBundle> dependencyManager;

	private Map<Long, NodeGroup> scheduledNodeGroups;

	private long serialNumberCounter = 1L;

	public ConcreteScheduler(ConcreteManager concreteManager) {
		this.concreteManager = concreteManager;
	}

	private void setApplicationSpecification(ApplicationSpecification applicationSpecification) {
		this.applicationSpecification = applicationSpecification;
	}

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

	private Launcher getRandomLauncher() throws InsufficientLaunchersException {
		Launcher launcher =  concreteManager.getRandomLauncher();

		if(launcher == null) {
			throw new InsufficientLaunchersException();
		}

		return launcher;
	}

	public synchronized boolean handleTermination(Long serialNumber) {
		NodeGroup terminated = scheduledNodeGroups.remove(serialNumber);

		if(terminated != null) {
			dependencyManager.removeDependency(terminated);

			return true;
		}

		return false;
	}

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

	public synchronized boolean finished() {
		return (!dependencyManager.hasLockedDependents() && !dependencyManager.hasFreeDependents() && (scheduledNodeGroups.size() == 0));
	}
}
