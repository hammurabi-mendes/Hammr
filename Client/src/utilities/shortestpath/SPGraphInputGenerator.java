package utilities.shortestpath;

import java.io.IOException;

import java.util.Random;

import java.util.Map;
import java.util.HashMap;

import java.util.List;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import utilities.GraphInputGenerator;

import utilities.filesystem.Directory;

import graphs.programs.shortestpath.SPGraphVertex;
import graphs.programs.shortestpath.SPGraphEdge;

public class SPGraphInputGenerator extends GraphInputGenerator<SPGraphVertex, SPGraphEdge> {
	private int numberVertices;

	private double probabilityEdges;

	private double maximumDistance;

	public SPGraphInputGenerator(Directory directory, int numberVertices, double probabilityEdges, double maximumDistance, String[] outputs) {
		super(directory, outputs);

		this.numberVertices = numberVertices;

		this.probabilityEdges = probabilityEdges;

		this.maximumDistance = maximumDistance;
	}

	protected void obtainGraph() {
		graph = new DefaultDirectedWeightedGraph<SPGraphVertex,SPGraphEdge>(SPGraphEdge.class);

		Random random = new Random();

		Map<Integer,SPGraphVertex> vertexMap = new HashMap<Integer,SPGraphVertex>();

		for(int i = 0; i < numberVertices; i++) {
			SPGraphVertex vertex = new SPGraphVertex();

			graph.addVertex(vertex);

			vertexMap.put(i, vertex);
		}

		for(int i = 0; i < numberVertices; i++) {
			for(int j = 0; j < numberVertices; j++) {
				double dice = random.nextDouble();

				if(dice <= probabilityEdges) {
					graph.addEdge(vertexMap.get(i), vertexMap.get(j), new SPGraphEdge(random.nextDouble() * maximumDistance));
				}
			}
		}	
	}

	public static void main(String[] arguments) {
		if(arguments.length <= 4) {
			System.err.println("usage: SPGraphInputGenerator baseDirectory numberVertices probabilityEdges maximumDistance [<input> ... <input>]");

			String baseDirectory = arguments[0];

			int numberVertices = Integer.valueOf(arguments[1]);

			double probabilityEdges = Double.valueOf(arguments[2]);

			double maximumDistance = Double.valueOf(arguments[3]);

			List<String> outputsList = new ArrayList<String>();

			for(int i = 4; i < arguments.length; i++) {
				outputsList.add(arguments[i]);
			}

			String[] outputsArray = outputsList.toArray(new String[outputsList.size()]);

			SPGraphInputGenerator generator = new SPGraphInputGenerator(new Directory(baseDirectory), numberVertices, probabilityEdges, maximumDistance, outputsArray);

			try {
				generator.run();
			} catch (IOException exception) {
				System.err.println("Error generating input");

				exception.printStackTrace();
			}
		}
	}
}
