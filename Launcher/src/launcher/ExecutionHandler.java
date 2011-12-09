/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package launcher;

import java.util.Iterator;

import java.util.Map;
import java.util.HashMap;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import java.net.InetSocketAddress;

import utilities.RMIHelper;

import communication.channel.InputChannel;
import communication.channel.OutputChannel;

import communication.channel.SHMInputChannel;
import communication.channel.SHMOutputChannel;

import communication.channel.TCPInputChannel;
import communication.channel.TCPOutputChannel;

import communication.channel.FileInputChannel;
import communication.channel.FileOutputChannel;

import communication.readers.FileChannelElementReader;
import communication.writers.FileChannelElementWriter;

import communication.readers.SHMChannelElementMultiplexer;
import communication.readers.TCPChannelElementMultiplexer;

import communication.writers.SHMChannelElementWriter;
import communication.writers.TCPChannelElementWriter;

import appspecs.Node;

import execinfo.NodeGroup;
import execinfo.ResultSummary;
import execinfo.NodeMeasurements;

import interfaces.Exportable;
import interfaces.Manager;

import exceptions.InexistentApplicationException;

import java.rmi.RemoteException;

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

		// Exports all the nodes that should be exported,
		// so their remote reference can be used externally

		for(Node node: nodeGroup.getNodes()) {
			if(node instanceof Exportable) {
				RMIHelper.exportRemoteObject((Exportable) node);
			}
		}

		try {
			setupCommunication();
		} catch (Exception genericException) {
			System.err.println("Error setting communication up for NodeGroup");

			genericException.printStackTrace();

			resultSummary = new ResultSummary(nodeGroup.getApplication(), nodeGroup.getSerialNumber(), ResultSummary.Type.FAILURE);

			for(Node node: nodeGroup.getNodes()) {
				resultSummary.addNodeMeasurements(node.getName(), null);
			}

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
		// Create all the pipe handlers (readers and writers)
		// If two pipe edges target the same node, only one pipe handler (and corresponding physical pipe) will be created

		Map<String, SHMChannelElementMultiplexer> mapChannelElementOutputStream = new HashMap<String, SHMChannelElementMultiplexer>();

		for(Node node: nodeGroup.getNodes()) {
			SHMChannelElementMultiplexer shmChannelElementMultiplexer = null;

			for(InputChannel inputChannel: node.getInputChannels()) {
				if(inputChannel instanceof SHMInputChannel) {
					SHMInputChannel shmInputChannel = (SHMInputChannel) inputChannel;

					if(shmChannelElementMultiplexer == null) {
						shmChannelElementMultiplexer = new SHMChannelElementMultiplexer(node.getInputChannelNames(SHMInputChannel.class));

						// For SHM, when creating the input pipe, map the associated output pipe for other nodes
						mapChannelElementOutputStream.put(node.getName(), shmChannelElementMultiplexer);
					}

					// For SHM, all the inputs come from the unique input pipe
					shmInputChannel.setChannelElementReader(shmChannelElementMultiplexer);
				}
			}
		}

		for(Node node: nodeGroup.getNodes()) {
			for(OutputChannel outputChannel: node.getOutputChannels()) {
				if(outputChannel instanceof SHMOutputChannel) {
					SHMOutputChannel shmOutputChannel = (SHMOutputChannel) outputChannel;

					// For SHM, all the outputs go to the unique output pipe for each node

					SHMChannelElementWriter shmChannelElementWriter = new SHMChannelElementWriter(node.getName(), mapChannelElementOutputStream.get(outputChannel.getName()));

					shmOutputChannel.setChannelElementWriter(shmChannelElementWriter);
				}
			}
		}

		// Create all the TCP handlers (readers and writers)
		// If two TCP edges target the same node, only one TCP handler (and corresponding server) will be created

		for(Node node: nodeGroup.getNodes()) {
			TCPChannelElementMultiplexer tcpChannelElementMultiplexer = null;

			for(InputChannel inputChannel: node.getInputChannels()) {
				if(inputChannel instanceof TCPInputChannel) {
					TCPInputChannel tcpInputChannel = (TCPInputChannel) inputChannel;

					if(tcpChannelElementMultiplexer == null) {
						tcpChannelElementMultiplexer = new TCPChannelElementMultiplexer(node.getInputChannelNames(TCPInputChannel.class));

						tcpInputChannel.setSocketAddress(tcpChannelElementMultiplexer.getAddress());

						// For TCP, when creating the input server, map the associated output server addresses for other nodes
						boolean result = manager.insertSocketAddress(nodeGroup.getApplication(), node.getName(), tcpInputChannel.getSocketAddress());

						if(result == false) {
							System.err.println("Unable to insert socket address for application " + nodeGroup.getApplication() + " on the manager!");

							throw new InexistentApplicationException(nodeGroup.getApplication());
						}
					}

					// For TCP, all the inputs come from the unique input server
					tcpInputChannel.setChannelElementReader(tcpChannelElementMultiplexer);
				}
			}
		}

		for(Node node: nodeGroup.getNodes()) {
			for(OutputChannel outputChannel: node.getOutputChannels()) {
				if(outputChannel instanceof TCPOutputChannel) {
					TCPOutputChannel tcpOutputChannel = (TCPOutputChannel) outputChannel;

					// For TCP, (1) obtain the address of the output server from the manager
					InetSocketAddress socketAddress = manager.obtainSocketAddress(nodeGroup.getApplication(), tcpOutputChannel.getName());

					if(socketAddress == null) {
						throw new InexistentApplicationException(nodeGroup.getApplication());
					}

					tcpOutputChannel.setSocketAddress(socketAddress);

					// For TCP, (2) all the outputs go to the unique server for each node

					TCPChannelElementWriter tcpChannelElementWriter = new TCPChannelElementWriter(node.getName(), socketAddress);

					tcpOutputChannel.setChannelElementWriter(tcpChannelElementWriter);
				}
			}
		}

		// Create all the file handlers (readers and writers)
		// If more than one file edge target the same node, more than one file handler (and corresponding file descriptor) will be created

		for(Node node: nodeGroup.getNodes()) {
			for(InputChannel inputChannel: node.getInputChannels()) {
				if(inputChannel instanceof FileInputChannel) {
					FileInputChannel fileInputChannel = (FileInputChannel) inputChannel;

					FileChannelElementReader fileChannelElementReader = new FileChannelElementReader(fileInputChannel.getFileInformation());

					fileInputChannel.setChannelElementReader(fileChannelElementReader);
				}
			}

			for(OutputChannel outputChannel: node.getOutputChannels()) {
				if(outputChannel instanceof FileOutputChannel) {
					FileOutputChannel fileOutputChannel = (FileOutputChannel) outputChannel;

					FileChannelElementWriter fileChannelElementWriter = new FileChannelElementWriter(fileOutputChannel.getFileInformation());

					fileOutputChannel.setChannelElementWriter(fileChannelElementWriter);
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
		NodeHandler[] nodeHandlers = new NodeHandler[nodeGroup.getSize()];

		Iterator<Node> iterator = nodeGroup.getNodesIterator();

		long globalTimerStart = System.currentTimeMillis();

		for(int i = 0; i < nodeGroup.getSize(); i++) {
			nodeHandlers[i] = new NodeHandler(iterator.next());

			nodeHandlers[i].start();
		}

		for(int i = 0; i < nodeGroup.getSize(); i++) {
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

		for(int i = 0; i < nodeGroup.getSize(); i++) {
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
