/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package graphs.programs;

import java.util.concurrent.TimeUnit;

import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.util.HashSet;

import java.util.List;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import communication.channel.ChannelElement;

import graphs.communication.EdgeChannelElement;
import graphs.communication.VertexChannelElement;

import nodes.TimedStatefulNode;

public abstract class GraphWorker<V extends GraphVertex,E extends GraphEdge> extends TimedStatefulNode {
	private static final long serialVersionUID = 1L;

	protected DefaultDirectedGraph<V,E> graph;

	protected int numberWorker;

	private int numberVertexes;
	private int numberWorkers;

	protected Map<String,V> vertexMap;
	protected Map<String,E> edgeMap;

	protected Map<String,Set<E>> foreignEdges;

	public GraphWorker(int numberWorker, int numberVertexes, int numberWorkers) {
		this(numberWorker, numberVertexes, numberWorkers, -1, null);
	}

	public GraphWorker(int numberWorker, int numberVertexes, int numberWorkers, int timeout, TimeUnit timeUnit) {
		super(timeout, timeUnit);

		this.numberWorker = numberWorker;

		this.numberVertexes = numberVertexes;
		this.numberWorkers = numberWorkers;

		this.vertexMap = new HashMap<String,V>();
		this.edgeMap = new HashMap<String,E>();

		this.foreignEdges = new HashMap<String,Set<E>>();
	}

	protected abstract DefaultDirectedGraph<V,E> createGraph();

	@SuppressWarnings("unchecked")
	protected void loadGraph() {
		// Only wait for closing outputs of the structural nodes

		createReaderShuffler(true, false);

		graph = createGraph();

		for(String applicationInput: getApplicationInputChannelNames()) {
			ChannelElement channelElement;

			List<V> vertexes = new ArrayList<V>();
			List<E> edges = new ArrayList<E>();

			while(true) {
				channelElement = read(applicationInput);

				if(channelElement == null) {
					break;
				}

				if(channelElement instanceof VertexChannelElement<?>) {
					vertexes.add(((VertexChannelElement<V>) channelElement).getObject());
				}

				if(channelElement instanceof EdgeChannelElement<?>) {
					edges.add(((EdgeChannelElement<E>) channelElement).getObject());
				}
			}

			// Add all vertices
			for(V vertex: vertexes) {
				graph.addVertex(vertex);

				vertexMap.put(vertex.getName(), vertex);
			}

			// Add all edges
			for(E edge: edges) {
				String sourceName = edge.getSourceName();
				String targetName = edge.getTargetName();

				V sourceVertex = vertexMap.get(sourceName);
				V targetVertex = vertexMap.get(targetName);

				// If this is a foreign neighbor, add the neighbor
				// to the foreign neighbor list. Otherwise, add the
				// edge to the local graph.

				if(targetVertex == null) {
					if(!foreignEdges.containsKey(sourceName)) {
						foreignEdges.put(sourceName, new HashSet<E>());
					}

					foreignEdges.get(sourceName).add(edge);
				}
				else {
					graph.addEdge(sourceVertex, targetVertex, edge);
				}

				edgeMap.put(edge.getName(), edge);
			}
		}
	}

	protected void dumpGraph() {
		for(String applicationOutput: getApplicationOutputChannelNames()) {
			BreadthFirstIterator<V,E> iterator = new BreadthFirstIterator<V,E>(graph);

			while(iterator.hasNext()) {
				V vertex = iterator.next();

				write(new VertexChannelElement<V>(vertex), applicationOutput);

				for(E edge: graph.outgoingEdgesOf(vertex)) {
					write(new EdgeChannelElement<E>(edge), applicationOutput);
				}
			}
		}
	}

	protected String obtainOwnerWorker(String vertexName) {
		return "worker-" + (Integer.valueOf(vertexName) / (numberVertexes / numberWorkers));
	}
}
