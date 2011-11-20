/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package appspecs;

import java.util.Iterator;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import org.jgrapht.graph.*;

import utilities.FileHelper;

import communication.channel.FileInputChannel;
import communication.channel.FileOutputChannel;
import communication.channel.SHMInputChannel;
import communication.channel.SHMOutputChannel;
import communication.channel.TCPInputChannel;
import communication.channel.TCPOutputChannel;


import enums.CommunicationType;
import exceptions.OverlapingFilesException;

public class ApplicationSpecification extends DefaultDirectedGraph<Node, Edge> {
	private static final long serialVersionUID = 1L;

	protected String name;
	protected String directoryPrefix;

	protected Map<String, Set<FileInputChannel>> inputs;
	protected Map<String, FileOutputChannel> outputs;


	protected Set<Node> fileConsumers;
	protected Set<Node> fileProducers;

	protected Decider decider;

	protected String nameGenerationString = "node-";
	protected long nameGenerationCounter = 0L;
	
	public ApplicationSpecification(String name, String poolName, String directoryPrefix) {
		super(Edge.class);

		this.name = name;
		this.directoryPrefix = directoryPrefix;

		fileConsumers = new HashSet<Node>();
		fileProducers = new HashSet<Node>();

		inputs = new HashMap<String, Set<FileInputChannel>>();
		outputs = new HashMap<String, FileOutputChannel>();
	}

	public ApplicationSpecification(String name, String directoryPrefix)
	{
		this(name, "default_pool", directoryPrefix);
	}
	
	public ApplicationSpecification() {
		this("default_application", "/userdata");
	}

	public ApplicationSpecification(String name, String directoryPrefix, Node[] nodes, Edge[] edges) {
		this(name, directoryPrefix);

		insertNodes(nodes);
		insertEdges(edges);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDirectoryPrefix(String directoryPrefix) {
		this.directoryPrefix = directoryPrefix;
	}

	public String getDirectoryPrefix() {
		return directoryPrefix;
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

	public void insertEdges(Edge[] edges) {
		for(Edge edge: edges) {
			addEdge(edge.getSource(), edge.getTarget());
		}
	}

	public void insertEdges(Node[] origins, Node[] destinations, CommunicationType communicationType) {
		insertEdges(origins, destinations, communicationType, -1);
	}

	public void insertEdges(Node[] origins, Node[] destinations, CommunicationType communicationType, Integer quantity) {
		Node currentOrigin = null;
		Node currentDestination = null;

		int destinationPosition = 0;

		for(int i = 0; i < origins.length; i++) {
			currentOrigin = origins[i];

			if(quantity == -1) {
				for(int j = 0; j < destinations.length; j++) {
					currentDestination = destinations[j];

					addEdge(currentOrigin, currentDestination, new Edge(communicationType));
				}
			}
			else {
				for(int j = 0; j < quantity; j++) {
					currentDestination = destinations[destinationPosition++ % destinations.length];

					addEdge(currentOrigin, currentDestination, new Edge(communicationType));
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
	
	public void addInput(Node node, String filename)
	{
		String absoluteFileName = getAbsoluteFileName(filename);

		if(inputs.get(absoluteFileName) == null) {
			inputs.put(absoluteFileName, new HashSet<FileInputChannel>());
		}

		
		FileInputChannel inputChannelHandler = new FileInputChannel(filename, absoluteFileName);

		node.addInputChannel(inputChannelHandler);

		inputs.get(absoluteFileName).add(inputChannelHandler);
		fileConsumers.add(node);
	}

	public Set<String> getInputFilenames() {
		return inputs.keySet();
	}

	public Set<Node> getFileConsumers() {
		return fileConsumers;
	}

	public void addOutput(Node node, String filename) throws OverlapingFilesException {
		String absoluteFileName = getAbsoluteFileName(filename);

		if (outputs.get(absoluteFileName) != null) {
			throw new OverlapingFilesException(absoluteFileName);
		}

		FileOutputChannel outputChannelHandler = new FileOutputChannel(filename, absoluteFileName);

		node.addOutputChannel(outputChannelHandler);

		outputs.put(absoluteFileName, outputChannelHandler);
		fileProducers.add(node);
	}
	
	public Set<String> getOutputFilenames() {
		return outputs.keySet();
	}

	public Set<Node> getFileProducers() {
		return fileProducers;
	}

	public String getAbsoluteDirectory() {
		return getDirectoryPrefix() + "/" + getName();
	}

	public String getAbsoluteFileName(String filename) {
		if(filename.startsWith("/")) {
			return filename;
		}

		return getAbsoluteDirectory() + "/" + filename;
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

	public void finalize() throws OverlapingFilesException {
		// Check if input or output filenames overlap

		for(String inputFilename: getInputFilenames()) {
			for(String outputFilename: getOutputFilenames()) {
				if(inputFilename.equals(outputFilename)) {
					throw new OverlapingFilesException(inputFilename);
				}
			}
		}

		long anonymousFileChannelCounter = 1000L;

		Node source, target;

		for(Edge edge: edgeSet()) {
			source = edge.getSource();
			target = edge.getTarget();

			switch(edge.getCommunicationMode()) {
			case SHM:
				source.addOutputChannel(new SHMOutputChannel(target.getName()));
				target.addInputChannel(new SHMInputChannel(source.getName()));
				break;
			case TCP:
				source.addOutputChannel(new TCPOutputChannel(target.getName()));
				target.addInputChannel(new TCPInputChannel(source.getName()));
				break;
			case FILE:
				String filePath = this.getAbsoluteDirectory() + "/" + "anonymous-filechannel-" + anonymousFileChannelCounter + ".dat";
				
				source.addOutputChannel(new FileOutputChannel(target.getName(), filePath));
				target.addInputChannel(new FileInputChannel(source.getName(), filePath));
				
				anonymousFileChannelCounter++;

				break;
			}
		}
		
	}

	public void relinkOutputsInputs() {
		List<String> inputFilenames = new ArrayList<String>(getInputFilenames());
		List<String> outputFilenames = new ArrayList<String>(getOutputFilenames());

		List<Node> listFileConsumers = new ArrayList<Node>(fileConsumers);

		Collections.shuffle(inputFilenames);
		Collections.shuffle(outputFilenames);

		Collections.shuffle(listFileConsumers);

		int minimum = Math.min(inputFilenames.size(), outputFilenames.size());

		for(int i = 0; i < minimum; i++) {
			FileHelper.move(outputFilenames.get(i), inputFilenames.get(i));
		}
	}
}
