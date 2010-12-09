package appspecs;

import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

import org.jgrapht.graph.*;

import utilities.FileHelper;

import communication.ChannelHandler;
import communication.FileChannelHandler;
import communication.SHMChannelHandler;
import communication.TCPChannelHandler;

import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;

public class ApplicationSpecification extends DefaultDirectedGraph<Node, Edge> {
	private static final long serialVersionUID = 1L;

	private Set<Node> initials;
	private Set<Node> finals;

	private String name;
	private String directoryPrefix;

	private Set<String> outputFilenames;

	private long inputCounter = 1L;
	private long outputCounter = 1L;

	protected String nameGenerationString = "node-";
	protected long nameGenerationCounter = 0L;

	public ApplicationSpecification(String name, String directoryPrefix) {
		super(Edge.class);

		this.name = name;
		this.directoryPrefix = directoryPrefix;

		initials = new HashSet<Node>();
		finals = new HashSet<Node>();

		outputFilenames = new HashSet<String>();
	}

	public ApplicationSpecification() {
		this("default_application", "/cluserdata");
	}

	public ApplicationSpecification(String name, String directoryPrefix, Node[] nodes, Edge[] edges) {
		this(name, directoryPrefix);

		insertNodes(nodes);
		insertEdges(edges);
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

	public void insertEdges(Node[] origins, Node[] destinations, EdgeType edgeType) {
		insertEdges(origins, destinations, edgeType, -1);
	}

	public void insertEdges(Node[] origins, Node[] destinations, EdgeType edgeType, Integer quantity) {
		Node currentOrigin = null;
		Node currentDestination = null;

		int destinationPosition = 0;

		for(int i = 0; i < origins.length; i++) {
			currentOrigin = origins[i];

			if(quantity == -1) {
				for(int j = 0; j < destinations.length; j++) {
					currentDestination = destinations[j];

					addEdge(currentOrigin, currentDestination, new Edge(edgeType));
				}
			}
			else {
				for(int j = 0; j < quantity; j++) {
					currentDestination = destinations[destinationPosition++ % destinations.length];

					addEdge(currentOrigin, currentDestination, new Edge(edgeType));
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

	public void addInitial(Node node, String filename) throws InexistentInputException {
		if(!FileHelper.exists(getAbsoluteFileName(filename))) {
			throw new InexistentInputException(getAbsoluteFileName(filename));
		}

		node.setType(NodeType.INITIAL);
		node.addInputChannelHandler(new FileChannelHandler(ChannelHandler.Mode.INPUT, "input-" + (inputCounter++), getAbsoluteFileName(filename)));

		initials.add(node);
	}

	public Set<Node> getInitials() {
		return initials;
	}

	public void setInitials(Set<Node> initials) {
		this.initials = initials;
	}

	public void addFinal(Node node, String filename) throws OverlappingOutputException {
		if(outputFilenames.contains(filename)) {
			throw new OverlappingOutputException(filename);
		}

		outputFilenames.add(filename);

		node.setType(NodeType.FINAL);
		node.addOutputChannelHandler(new FileChannelHandler(ChannelHandler.Mode.OUTPUT, "output-" + (outputCounter++), getAbsoluteFileName(filename)));

		finals.add(node);
	}

	public Set<Node> getFinals() {
		return finals;
	}

	public void setFinals(Set<Node> finals) {
		this.finals = finals;
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

	public boolean initialize() {
		return FileHelper.exists(getAbsoluteDirectory());
	}

	public String generateUniqueName() {
		return nameGenerationString + (nameGenerationCounter++);
	}

	public void finalize() {
		long anonymousFileChannelCounter = 1000L;

		Node source, target;

		for(Edge edge: edgeSet()) {
			source = edge.getSource();
			target = edge.getTarget();

			switch(edge.getCommunicationMode()) {
			case SHM:
				source.addOutputChannelHandler(new SHMChannelHandler(ChannelHandler.Mode.OUTPUT, target.getName()));
				target.addInputChannelHandler(new SHMChannelHandler(ChannelHandler.Mode.INPUT, source.getName()));
				break;
			case TCP:
				source.addOutputChannelHandler(new TCPChannelHandler(ChannelHandler.Mode.OUTPUT, target.getName()));
				target.addInputChannelHandler(new TCPChannelHandler(ChannelHandler.Mode.INPUT, source.getName()));
				break;
			case FILE:
				source.addOutputChannelHandler(new FileChannelHandler(ChannelHandler.Mode.OUTPUT, target.getName(), this.getAbsoluteDirectory() + "/" + "anonymous-filechannel-" + anonymousFileChannelCounter + ".dat"));
				target.addInputChannelHandler(new FileChannelHandler(ChannelHandler.Mode.INPUT, source.getName(), this.getAbsoluteDirectory() + "/" + "anonymous-filechannel-" + anonymousFileChannelCounter + ".dat"));

				anonymousFileChannelCounter++;

				break;
			}
		}
	}
}
