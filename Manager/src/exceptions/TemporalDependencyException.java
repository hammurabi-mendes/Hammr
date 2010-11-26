package exceptions;

import appspecs.Node;

public class TemporalDependencyException extends Exception {
	private static final long serialVersionUID = 1L;

	private Node node1;
	private Node node2;

	public TemporalDependencyException(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public String toString() {
		return "Nodes " + node1 +  " and " + node2 + " represent an invalid temporal dependency exception";
	}
}
