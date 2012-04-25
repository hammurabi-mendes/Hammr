/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package appspecs;

import java.io.Serializable;

import java.util.Iterator;

import java.util.Collections;

import java.util.Set;
import java.util.HashSet;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import org.jgrapht.graph.*;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Directory;
import utilities.filesystem.Filename;
import utilities.filesystem.Protocol;

import security.authenticators.Authenticator;
import security.restrictions.LauncherRestrictions;

import communication.channel.InputChannel;
import communication.channel.OutputChannel;

import communication.channel.SHMInputChannel;
import communication.channel.SHMOutputChannel;

import communication.channel.FileInputChannel;
import communication.channel.FileOutputChannel;

import communication.channel.TCPInputChannel;
import communication.channel.TCPOutputChannel;

import enums.CommunicationMode;

import exceptions.OverlapingFilesException;

import interfaces.ApplicationAggregator;
import interfaces.ApplicationController;

public class ApplicationSpecification extends DefaultDirectedGraph<Node, Edge> {
	private static final long serialVersionUID = 1L;

	protected String name;

	protected Authenticator userAuthenticator;
	protected Authenticator applicationAuthenticator;

	protected Directory baseDirectory;

	protected Map<Filename, Set<Node>> inputToNodes;
	protected Map<Filename, Node> outputToNodes;

	protected Map<Filename, Set<InputChannel>> inputToChannels;
	protected Map<Filename, OutputChannel> outputToChannels;

	protected Map<Node, Set<Filename>> nodeToInputs;
	protected Map<Node, Set<Filename>> nodeToOutputs;

	protected Set<Node> initials;

	protected Decider decider;

	protected Map<String, ApplicationAggregator<? extends Serializable,? extends Serializable>> aggregators;

	protected Map<String, ApplicationController> controllers;

	protected String nameGenerationString = "node-";

	protected long nameGenerationCounter = 0L;

	protected long anonymousFileChannelCounter = 0L;

	protected long relinkedFileChannelCounter = 0L;

	protected LauncherRestrictions globalLauncherRestrictions;

	protected Map<Node, LauncherRestrictions> nodeRestrictions;

	protected Map<Node, Authenticator> nodeAuthenticators;

	public ApplicationSpecification(String name, Directory baseDirectory) {
		super(Edge.class);

		this.name = name;
		this.baseDirectory = baseDirectory;

		this.inputToNodes = new HashMap<Filename, Set<Node>>();
		this.outputToNodes = new HashMap<Filename, Node>();

		this.inputToChannels = new HashMap<Filename, Set<InputChannel>>();
		this.outputToChannels = new HashMap<Filename, OutputChannel>();

		this.nodeToInputs = new HashMap<Node, Set<Filename>>();
		this.nodeToOutputs = new HashMap<Node, Set<Filename>>();

		this.initials = new HashSet<Node>();

		this.aggregators = new HashMap<String, ApplicationAggregator<? extends Serializable,? extends Serializable>>();
		this.controllers = new HashMap<String, ApplicationController>();

		this.nodeRestrictions = new HashMap<Node, LauncherRestrictions>();
		this.nodeAuthenticators = new HashMap<Node, Authenticator>();
	}

	public ApplicationSpecification() {
		this("default_application", FileHelper.getDirectoryInformation("/tmp/userdata", Protocol.POSIX_COMPATIBLE));
	}

	public ApplicationSpecification(String name, Directory baseDirectory, Node[] nodes, Edge[] edges) {
		this(name, baseDirectory);

		insertNodes(nodes);
		insertEdges(edges);
	}

	public String getName() {
		return name;
	}

	public Authenticator getUserAuthenticator() {
		return userAuthenticator;
	}

	public void setUserAuthenticator(Authenticator authenticator) {
		this.userAuthenticator = authenticator;
	}

	public Authenticator getApplicationAuthenticator() {
		return applicationAuthenticator;
	}

	public void setApplicationAuthenticator(Authenticator authenticator) {
		this.applicationAuthenticator = authenticator;
	}

	public Decider getDecider() {
		return decider;
	}

	public void setDecider(Decider decider) {
		this.decider = decider;
	}

	public void insertNodes(Node[] nodes) {
		for(Node node: nodes) {
			node.setName(generateUniqueName());

			addVertex(node);
		}
	}

	public void setInitials(Node[] nodes) {
		for(Node node: nodes) {
			initials.add(node);
		}
	}

	public Set<Node> getInitials() {
		return initials;
	}

	public void insertEdges(Edge[] edges) {
		for(Edge edge: edges) {
			addEdge(edge.getSource(), edge.getTarget());
		}
	}

	public void insertEdges(Node[] origins, Node[] destinations, CommunicationMode communicationMode) {
		insertEdges(origins, destinations, communicationMode, -1);
	}

	public void insertEdges(Node[] origins, Node[] destinations, CommunicationMode communicationMode, Integer quantity) {
		Node currentOrigin = null;
		Node currentDestination = null;

		int destinationPosition = 0;

		for(int i = 0; i < origins.length; i++) {
			currentOrigin = origins[i];

			if(quantity == -1) {
				for(int j = 0; j < destinations.length; j++) {
					currentDestination = destinations[j];

					if(currentDestination != currentOrigin) {
						addEdge(currentOrigin, currentDestination, new Edge(communicationMode));
					}
				}
			}
			else {
				for(int j = 0; j < quantity; j++) {
					currentDestination = destinations[destinationPosition++ % destinations.length];

					if(currentDestination != currentOrigin) {
						addEdge(currentOrigin, currentDestination, new Edge(communicationMode));
					}
				}
			}
		}
	}

	public void incorporateGraph(ApplicationSpecification other) {
		insertNodes((Node[]) other.vertexSet().toArray());

		insertEdges((Edge[]) other.edgeSet().toArray());
	}

	public void incorporateGraphs(ApplicationSpecification... others) {
		for(ApplicationSpecification other: others) {
			incorporateGraph(other);
		}
	}

	public void addInput(Node node, Filename filename) {
		if(inputToChannels.get(filename) == null) {
			inputToChannels.put(filename, new HashSet<InputChannel>());
		}

		if(inputToNodes.get(filename) == null) {
			inputToNodes.put(filename, new HashSet<Node>());
		}

		if(nodeToInputs.get(node) == null) {
			nodeToInputs.put(node, new HashSet<Filename>());
		}

		FileInputChannel inputChannel = new FileInputChannel(filename.getLocation(), filename);

		node.addInputChannel(filename.getLocation(), inputChannel, true);

		inputToChannels.get(filename).add(inputChannel);
		inputToNodes.get(filename).add(node);

		nodeToInputs.get(node).add(filename);
	}

	public InputChannel delInput(Node node, Filename filename) {
		InputChannel inputChannel = node.delInputChannel(filename.getLocation());

		if(inputChannel == null) {
			return null;
		}

		inputToChannels.get(filename).remove(inputChannel);
		inputToNodes.get(filename).remove(node);

		nodeToInputs.get(node).remove(filename);

		if(inputToChannels.get(filename).size() == 0) {
			inputToChannels.remove(filename);
		}

		if(inputToNodes.get(filename).size() == 0) {
			inputToNodes.remove(filename);
		}

		if(nodeToInputs.get(node).size() == 0) {
			nodeToInputs.remove(node);
		}

		return inputChannel;
	}

	public Set<InputChannel> delInput(Filename filename) {
		Set<InputChannel> result = new HashSet<InputChannel>();

		for(Node node: getFileConsumers()) {
			if(node.getInputChannel(filename.getLocation()) != null) {
				InputChannel inputChannel = delInput(node, filename);

				result.add(inputChannel);
			}
		}

		return result;
	}

	public Set<Filename> getInputFilenames() {
		return inputToNodes.keySet();
	}

	public Set<Node> getFileConsumers() {
		return nodeToInputs.keySet();
	}

	public void addOutput(Node node, Filename filename) throws OverlapingFilesException {
		if(outputToChannels.get(filename) != null) {
			throw new OverlapingFilesException(filename);
		}

		if(outputToNodes.get(filename) != null) {
			throw new OverlapingFilesException(filename);
		}

		if(nodeToOutputs.get(node) == null) {
			nodeToOutputs.put(node, new HashSet<Filename>());
		}

		FileOutputChannel outputChannel = new FileOutputChannel(filename.getLocation(), filename);

		node.addOutputChannel(filename.getLocation(), outputChannel, true);

		outputToChannels.put(filename, outputChannel);
		outputToNodes.put(filename, node);

		nodeToOutputs.get(node).add(filename);
	}

	public OutputChannel delOutput(Node node, Filename filename) {
		OutputChannel outputChannel = node.delOutputChannel(filename.getLocation());

		if(outputChannel == null) {
			return null;
		}

		outputToChannels.remove(filename);
		outputToNodes.remove(filename);

		nodeToOutputs.get(node).remove(filename);

		if(nodeToOutputs.get(node).size() == 0) {
			nodeToOutputs.remove(node);
		}

		return outputChannel;
	}

	public Set<OutputChannel> delOutput(Filename filename) {
		Set<OutputChannel> result = new HashSet<OutputChannel>();

		for(Node node: getFileProducers()) {
			if(node.getOutputChannel(filename.getLocation()) != null) {
				OutputChannel outputChannel = delOutput(node, filename);

				result.add(outputChannel);
			}
		}

		return result;
	}

	public Set<Filename> getOutputFilenames() {
		return outputToNodes.keySet();
	}

	public Set<Node> getFileProducers() {
		return nodeToOutputs.keySet();
	}

	public Iterator<Node> nodeIterator() {
		return vertexSet().iterator();
	}

	public Iterator<Edge> edgeIterator() {
		return edgeSet().iterator();
	}

	public Set<Node> getNeighbors(Node node) {
		Set<Node> result = new HashSet<Node>();

		for(Edge edge: outgoingEdgesOf(node)) {
			result.add(edge.getTarget());
		}

		return result;
	}

	public Set<Edge> getConnections(Node node) {
		return outgoingEdgesOf(node);
	}

	public String generateUniqueName() {
		return nameGenerationString + (nameGenerationCounter++);
	}

	public ApplicationAggregator<? extends Serializable,? extends Serializable> addAggregator(String variable, ApplicationAggregator<? extends Serializable,? extends Serializable> aggregator) {
		return aggregators.put(variable, aggregator);
	}

	public ApplicationAggregator<? extends Serializable,? extends Serializable> getAggregator(String variable) {
		return aggregators.get(variable);
	}

	public Map<String, ApplicationAggregator<? extends Serializable,? extends Serializable>> getAggregators() {
		return aggregators;
	}

	public ApplicationController addController(String name, ApplicationController controller) {
		return controllers.put(name, controller);
	}

	public ApplicationController getController(String name) {
		return controllers.get(name);
	}

	public Map<String, ApplicationController> getControllers() {
		return controllers;
	}

	public LauncherRestrictions getGlobalRestrictions() {
		return globalLauncherRestrictions;
	}

	public void setGlobalRestrictions(LauncherRestrictions restrictions) {
		this.globalLauncherRestrictions = restrictions;
	}

	public void insertNodeRestriction(Node node, LauncherRestrictions restrictions) {
		nodeRestrictions.put(node, restrictions);
	}

	public LauncherRestrictions obtainNodeRestriction(Node node) {
		return nodeRestrictions.get(node);
	}

	public void removeNodeRestriction(Node node) {
		nodeRestrictions.remove(node);
	}

	public void insertNodeAuthenticator(Node node, Authenticator authenticator) {
		nodeAuthenticators.put(node, authenticator);
	}

	public Authenticator obtainNodeAuthenticator(Node node) {
		return nodeAuthenticators.get(node);
	}

	public void removeNodeAuthenticator(Node node) {
		nodeAuthenticators.remove(node);
	}

	public Map<Node,Authenticator> getNodeAuthenticators() {
		return nodeAuthenticators;
	}

	public void finalize() throws OverlapingFilesException {
		// Check if input or output filenames overlap

		for(Filename inputFilename: getInputFilenames()) {
			for(Filename outputFilename: getOutputFilenames()) {
				if(inputFilename.equals(outputFilename)) {
					throw new OverlapingFilesException(inputFilename);
				}
			}
		}

		Node source, target;

		for(Edge edge: edgeSet()) {
			source = edge.getSource();
			target = edge.getTarget();

			switch(edge.getCommunicationMode()) {
			case SHM:
				source.addOutputChannel(target.getName(), new SHMOutputChannel(target.getName()), false);
				target.addInputChannel(source.getName(), new SHMInputChannel(source.getName()), false);
				break;
			case TCP:
				source.addOutputChannel(target.getName(), new TCPOutputChannel(target.getName()), false);
				target.addInputChannel(source.getName(), new TCPInputChannel(source.getName()), false);
				break;
			case FILE:
				Filename filename = edge.getFilename(); 

				// If a filename was not set, create an anonymous filename
				if(filename == null) {
					filename = FileHelper.getFileInformation(baseDirectory.getPath(), "anonymous-filechannel-" + (anonymousFileChannelCounter++) + ".dat", baseDirectory.getProtocol());
				}

				source.addOutputChannel(target.getName(), new FileOutputChannel(target.getName(), filename), false);
				target.addInputChannel(source.getName(), new FileInputChannel(source.getName(), filename), false);

				break;
			}
		}
	}

	public void relinkOutputsInputs() {
		List<Filename> inputFilenames = new ArrayList<Filename>(getInputFilenames());
		List<Filename> outputFilenames = new ArrayList<Filename>(getOutputFilenames());

		Collections.shuffle(inputFilenames);
		Collections.shuffle(outputFilenames);

		int minimum = Math.min(inputFilenames.size(), outputFilenames.size());

		// Rename the outputs to the inputs

		for(int i = 0; i < minimum; i++) {
			FileHelper.move(outputFilenames.get(i), inputFilenames.get(i));
		}

		// Delete the excess inputs

		// Keep track of nodes who were consumers before the operation
		// and the nodes who were consumers after the operation

		if(inputFilenames.size() > outputFilenames.size()) {
			Set<Node> consumersBefore = nodeToInputs.keySet();

			for(int i = minimum; i < inputFilenames.size(); i++) {
				delInput(inputFilenames.get(i));
			}

			Set<Node> consumersAfter = nodeToInputs.keySet();

			for(Node consumerBefore: consumersBefore) {
				// If the node was a consumer before, but the node is not a consumer after,
				// fake it an input of /dev/null for the next iteration

				if(!consumersAfter.contains(consumerBefore)) {
					addInput(consumerBefore, FileHelper.getFileInformation(baseDirectory.getPath(), "/dev/null", baseDirectory.getProtocol()));
				}
			}
		}

		// Distribute the excess outputs randomly between the previous consumers

		if(outputFilenames.size() > inputFilenames.size()) {
			List<Node> consumersBefore = new ArrayList<Node>(getFileConsumers());

			Collections.shuffle(consumersBefore);

			int indexConsumersBefore = 0;

			for(int i = minimum; i < outputFilenames.size(); i++) {
				Filename filename = FileHelper.getFileInformation(baseDirectory.getPath(), "relinked-filechannel-" + (relinkedFileChannelCounter++) + ".dat", baseDirectory.getProtocol());

				FileHelper.move(outputFilenames.get(i), filename);

				addInput(consumersBefore.get((indexConsumersBefore++) % consumersBefore.size()), filename);
			}
		}
	}
}
