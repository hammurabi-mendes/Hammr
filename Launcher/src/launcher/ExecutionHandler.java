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

/**
 * This class is responsible for running a specific NodeGroup previously submitted to the Launcher.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ExecutionHandler extends Thread {
	private Manager manager;

	private ConcreteLauncher concreteLauncher;

	private NodeGroup nodeGroup;

	/**
	 * Constructor.
	 * 
	 * @param manager Reference to the manager.
	 * @param concreteLauncher Reference to the local launcher.
	 * @param nodeGroup NodeGroup that should be run.
	 */
	public ExecutionHandler(Manager manager, ConcreteLauncher concreteLauncher, NodeGroup nodeGroup) {
		this.manager = manager;

		this.concreteLauncher = concreteLauncher;

		this.nodeGroup = nodeGroup;
	}

	/**
	 * Setter for the NodeGroup that should be run.
	 * 
	 * @param nodeGroup NodeGroup that should be run.
	 */
	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	/**
	 * Getter for the NodeGroup that should be run.
	 * 
	 * @return NodeGroup that should be run.
	 */
	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	/**
	 * Runs the NodeGroup in separate threads, one thread for each Node.
	 */
	public void run() {
		// Stores runtime information; sent back to the master
		// at the end of the execution.
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

		resultSummary = performExecution();

		finishExecution(resultSummary);
	}

	/**
	 * Creates the communication channels for the NodeGroup being run.
	 * If it has a server-side TCP channel, it notifies the master about the obtained socket address.
	 * If it has a client-side TCP channel, it obtains from the master the associated socket address.
	 * 
	 * @throws Exception If one of the following situations occur:
	 *         1) Error creating client-side or server-side TCP channels;
	 *         2) The address of a server-side TCP channel required by this NodeGroup is not located at the master;
	 *         3) Error creating or opening file channels.
	 */
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

	/**
	 * Performs the execution of the Nodes, one per thread, and prepares the result summary to send back to the master.
	 * 
	 * @return Result summary to send back to the master.
	 */
	private ResultSummary performExecution() {
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

	/**
	 * Sends the result summary of the NodeGroup back to the master, and clears the NodeGroup data from the launcher.
	 * 
	 * @param resultSummary Result summary obtained after the NodeGroup was executed.
	 * 
	 * @return True if the master was properly notified; false otherwise.
	 */
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

	/**
	 * Class that executes a single Node in a separate thread, performing the appopriate measurements.
	 * 
	 * @author Hammurabi Mendes (hmendes)
	 */
	class NodeHandler extends Thread {
		private Node node;

		private long realLocalTimerStart;
		private long realLocalTimerFinish;

		private long cpuLocalTimerStart;
		private long cpuLocalTimerFinish;

		private long userLocalTimerStart;
		private long userLocalTimerFinish;

		/**
		 * Class constructor.
		 * 
		 * @param node Node to be run in a separate thread.
		 */
		public NodeHandler(Node node) {
			this.node = node;
		}

		/**
		 * Runs the Node in a separate thread of execution, and obtain runtime measurements.
		 */
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

		/**
		 * Getter for the Node being run.
		 * 
		 * @return The node being run.
		 */
		public Node getNode() {
			return node;
		}

		/**
		 * Getter for the real time to execute the Node.
		 * 
		 * @return The real time to execute the Node.
		 */
		public long getRealTime() {
			return realLocalTimerFinish - realLocalTimerStart;
		}

		/**
		 * Getter for the CPU time to execute the Node.
		 * 
		 * @return The CPU time to execute the Node.
		 */
		public long getCpuTime() {
			// We get results in milliseconds, not in nanoseconds
			
			return (cpuLocalTimerFinish - cpuLocalTimerStart) / 1000000;
		}

		/**
		 * Getter for the user time to execute the Node.
		 * 
		 * @return The user time to execute the Node.
		 */
		public long getUserTime() {
			// We get results in milliseconds, not in nanoseconds
			
			return (userLocalTimerFinish - userLocalTimerStart) / 1000000;
		}

		/**
		 * Getter for the whole set of node measurements.
		 * 
		 * @return The whole set of node measurements.
		 */
		public NodeMeasurements getNodeMeasurements() {
			return new NodeMeasurements(getRealTime(), getCpuTime(), getUserTime());
		}
	}
}
