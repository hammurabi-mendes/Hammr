package exceptions;

import appspecs.Node;

/**
 * This class represent a temporal dependency error. This happens when node1 should execute
 * before node2 (because, say, node1 produces a file that should be consumed by node2), but
 * also node2 should execute before node1.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class TemporalDependencyException extends Exception {
	private static final long serialVersionUID = 1L;

	private Node node1;
	private Node node2;

	/**
	 * Constructor method.
	 * 
	 * @param node1 First node involved in the dependency error.
	 * @param node2 Second node involved in the dependency error.
	 */
	public TemporalDependencyException(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public String toString() {
		return "Nodes " + node1 +  " and " + node2 + " represent an invalid temporal dependency exception";
	}
}
