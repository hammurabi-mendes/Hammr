package launcher;

import java.util.Iterator;

import java.util.Map;
import java.util.HashMap;

import java.rmi.RemoteException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import java.net.InetSocketAddress;

import appspecs.Node;

import execinfo.NodeGroup;
import execinfo.ResultSummary;
import execinfo.NodeMeasurements;

import communication.ChannelHandler;

import communication.SHMChannelHandler;
import communication.TCPChannelHandler;
import communication.FileChannelHandler;

import communication.SHMChannelElementMultiplexer;
import communication.SHMChannelElementWriter;

import communication.TCPChannelElementMultiplexer;
import communication.TCPChannelElementWriter;

import communication.FileChannelElementReader;
import communication.FileChannelElementWriter;

import interfaces.Manager;

import exceptions.InexistentApplicationException;

public class ExecutionHandler extends Thread {
	private Manager manager;

	private ConcreteLauncher concreteLauncher;

	private NodeGroup nodeGroup;

	public ExecutionHandler(Manager manager, ConcreteLauncher concreteLauncher, NodeGroup nodeGroup) {
		this.manager = manager;

		this.concreteLauncher = concreteLauncher;

		this.nodeGroup = nodeGroup;
	}

	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	public void run() {
		ResultSummary resultSummary;

		try {
			setupCommunication();
		} catch (Exception genericException) {
			System.err.println("Error setting communication up for NodeGroup");

			genericException.printStackTrace();

			resultSummary = new ResultSummary(nodeGroup.getApplication(), nodeGroup.getSerialNumber(), ResultSummary.Type.FAILURE);

			finishExecution(resultSummary);

			return;
		}

		resultSummary = startExecution();

		finishExecution(resultSummary);
	}

	private void setupCommunication() throws Exception {
		// Create all the pipe handlers
		// If two pipe edges target the same node, only one pipe handler (and corresponding physical pipe) will be created

		Map<String, SHMChannelElementMultiplexer> mapChannelElementOutputStream = new HashMap<String, SHMChannelElementMultiplexer>();

		for(Node node: nodeGroup.getNodes()) {
			SHMChannelElementMultiplexer shmChannelElementMultiplexer = null;

			for(ChannelHandler channelHandler: node.getInputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.SHM) {
					SHMChannelHandler shmChannelHandler = (SHMChannelHandler) channelHandler;

					if(shmChannelElementMultiplexer == null) {
						shmChannelElementMultiplexer = new SHMChannelElementMultiplexer(node.getInputChannelNames());

						// For SHM, when creating the input pipe, map the associated output pipe for other nodes
						mapChannelElementOutputStream.put(node.getName(), shmChannelElementMultiplexer);
					}

					// For SHM, all the inputs come from the unique input pipe
					shmChannelHandler.setChannelElementReader(shmChannelElementMultiplexer);
				}
			}
		}

		for(Node node: nodeGroup.getNodes()) {
			for(ChannelHandler channelHandler: node.getOutputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.SHM) {
					SHMChannelHandler shmChannelHandler = (SHMChannelHandler) channelHandler;

					// For SHM, all the outputs go to the unique output pipe for each node

					SHMChannelElementWriter shmChannelElementWriter = new SHMChannelElementWriter(node.getName(), mapChannelElementOutputStream.get(channelHandler.getName()));

					shmChannelHandler.setChannelElementWriter(shmChannelElementWriter);
				}
			}
		}

		// Create all the TCP handlers
		// If two TCP edges target the same node, only one TCP handler (and corresponding server) will be created

		for(Node node: nodeGroup.getNodes()) {
			TCPChannelElementMultiplexer tcpChannelElementMultiplexer = null;

			for(ChannelHandler channelHandler: node.getInputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.TCP) {
					TCPChannelHandler tcpChannelHandler = (TCPChannelHandler) channelHandler;

					if(tcpChannelElementMultiplexer == null) {
						tcpChannelElementMultiplexer = new TCPChannelElementMultiplexer(node.getInputChannelNames());

						tcpChannelHandler.setSocketAddress(tcpChannelElementMultiplexer.getAddress());

						// For TCP, when creating the input server, map the associated output server addresses for other nodes
						boolean result = manager.insertSocketAddress(nodeGroup.getApplication(), node.getName(), tcpChannelHandler.getSocketAddress());

						if(result == false) {
							System.err.println("Unable to insert socket address for application " + nodeGroup.getApplication() + " on the manager!");

							throw new InexistentApplicationException(nodeGroup.getApplication());
						}
					}

					// For TCP, all the inputs come from the unique input server
					tcpChannelHandler.setChannelElementReader(tcpChannelElementMultiplexer);
				}
			}
		}

		for(Node node: nodeGroup.getNodes()) {
			for(ChannelHandler channelHandler: node.getOutputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.TCP) {
					TCPChannelHandler tcpChannelHandler = (TCPChannelHandler) channelHandler;

					// For TCP, (1) obtain the address of the output server from the manager
					InetSocketAddress socketAddress = manager.obtainSocketAddress(nodeGroup.getApplication(), tcpChannelHandler.getName());

					if(socketAddress == null) {
						throw new InexistentApplicationException(nodeGroup.getApplication());
					}

					tcpChannelHandler.setSocketAddress(socketAddress);

					// For TCP, (2) all the outputs go to the unique server for each node

					TCPChannelElementWriter tcpChannelElementWriter = new TCPChannelElementWriter(node.getName(), socketAddress);

					tcpChannelHandler.setChannelElementWriter(tcpChannelElementWriter);
				}
			}
		}

		// Create all the file handlers
		// If more than one file edge target the same node, more than one file handler (and corresponding file descriptor) will be created

		for(Node node: nodeGroup.getNodes()) {
			for(ChannelHandler channelHandler: node.getInputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.FILE) {
					FileChannelHandler fileChannelHandler = (FileChannelHandler) channelHandler;

					FileChannelElementReader fileChannelElementReader = new FileChannelElementReader(fileChannelHandler.getLocation());

					fileChannelHandler.setChannelElementReader(fileChannelElementReader);
				}
			}

			for(ChannelHandler channelHandler: node.getOutputChannelHandlers()) {
				if(channelHandler.getType() == ChannelHandler.Type.FILE) {
					FileChannelHandler fileChannelHandler = (FileChannelHandler) channelHandler;

					FileChannelElementWriter fileChannelElementWriter = new FileChannelElementWriter(fileChannelHandler.getLocation());

					fileChannelHandler.setChannelElementWriter(fileChannelElementWriter);
				}
			}
		}
	}

	private ResultSummary startExecution() {
		NodeHandler[] nodeHandlers = new NodeHandler[nodeGroup.size()];

		Iterator<Node> iterator = nodeGroup.iterator();

		long globalTimerStart = System.currentTimeMillis();

		for(int i = 0; i < nodeGroup.size(); i++) {
			nodeHandlers[i] = new NodeHandler(iterator.next());

			nodeHandlers[i].start();
		}

		for(int i = 0; i < nodeGroup.size(); i++) {
			try {
				nodeHandlers[i].join();
			} catch (InterruptedException exception) {
				System.err.println("Unexpected thread interruption while waiting for node execution termination");

				i--;
				continue;
			}
		}

		long globalTimerFinish = System.currentTimeMillis();

		ResultSummary resultSummary = new ResultSummary(nodeGroup.getApplication(), nodeGroup.getSerialNumber(), ResultSummary.Type.SUCCESS);

		resultSummary.setNodeGroupTiming(globalTimerFinish - globalTimerStart);

		for(int i = 0; i < nodeGroup.size(); i++) {
			resultSummary.addNodeMeasurements(nodeHandlers[i].getNode().getName(), nodeHandlers[i].getNodeMeasurements());
		}

		return resultSummary;
	}

	private boolean finishExecution(ResultSummary resultSummary) {
		concreteLauncher.delNodeGroup(nodeGroup);

		try {
			manager.handleTermination(resultSummary);
		} catch (RemoteException exception) {
			System.err.println("Unable to communicate termination to the manager");

			exception.printStackTrace();
			return false;
		}	

		return true;
	}

	class NodeHandler extends Thread {
		private Node node;

		private long realLocalTimerStart;
		private long realLocalTimerFinish;

		private long cpuLocalTimerStart;
		private long cpuLocalTimerFinish;

		private long userLocalTimerStart;
		private long userLocalTimerFinish;

		public NodeHandler(Node node) {
			this.node = node;
		}

		public void run() {
			System.out.println("Executing " + node);

			ThreadMXBean profiler = ManagementFactory.getThreadMXBean();

			if(profiler.isThreadCpuTimeSupported()) {
				if(!profiler.isThreadCpuTimeEnabled()) {
					profiler.setThreadCpuTimeEnabled(true);
				}
			}

			realLocalTimerStart = System.currentTimeMillis();

			cpuLocalTimerStart = profiler.getCurrentThreadCpuTime();
			userLocalTimerStart = profiler.getCurrentThreadUserTime();

			node.run();

			realLocalTimerFinish = System.currentTimeMillis();

			cpuLocalTimerFinish = profiler.getCurrentThreadCpuTime();
			userLocalTimerFinish = profiler.getCurrentThreadUserTime();
		}

		public Node getNode() {
			return node;
		}

		public long getRealTime() {
			return realLocalTimerFinish - realLocalTimerStart;
		}

		public long getCpuTime() {
			// We get results in milliseconds, not in nanoseconds
			
			return (cpuLocalTimerFinish - cpuLocalTimerStart) / 1000000;
		}

		public long getUserTime() {
			// We get results in milliseconds, not in nanoseconds
			
			return (userLocalTimerFinish - userLocalTimerStart) / 1000000;
		}

		public NodeMeasurements getNodeMeasurements() {
			return new NodeMeasurements(getRealTime(), getCpuTime(), getUserTime());
		}
	}
}
