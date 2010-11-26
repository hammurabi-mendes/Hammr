package appspecs;

import org.jgrapht.graph.DefaultEdge;

public class Edge extends DefaultEdge {
	private static final long serialVersionUID = 1L;

	private EdgeType type;

	public Edge(EdgeType type) {
		setCommunicationMode(type);
	}

	public Node getSource() {
		return (Node) super.getSource();
	}

	public Node getTarget() {
		return (Node) super.getTarget();
	}

	public void setCommunicationMode(EdgeType type) {
		this.type = type;
	}

	public EdgeType getCommunicationMode() {
		return type;
	}
}
