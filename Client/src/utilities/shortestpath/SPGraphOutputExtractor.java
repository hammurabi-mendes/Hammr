package utilities.shortestpath;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import utilities.GraphOutputExtractor;

import utilities.filesystem.Directory;

import graphs.programs.shortestpath.SPGraphVertex;
import graphs.programs.shortestpath.SPGraphEdge;

public class SPGraphOutputExtractor extends GraphOutputExtractor<SPGraphVertex,SPGraphEdge> {
	public SPGraphOutputExtractor(Directory directory, String[] inputs) {
		super(directory, inputs);
	}

	protected void createGraph() {
		graph = new DefaultDirectedWeightedGraph<SPGraphVertex,SPGraphEdge>(SPGraphEdge.class);
	}

	protected void printGraph() {
		for(SPGraphVertex vertex: graph.vertexSet()) {
			System.out.println(vertex);
		}

		for(SPGraphEdge edge: graph.edgeSet()) {
			System.out.println(edge);
		}
	}
}
