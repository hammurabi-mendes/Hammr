/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package graphs.programs.shortestpath;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import org.jgrapht.graph.DefaultDirectedGraph;

import graphs.programs.GraphWorker;

import interfaces.Aggregator;

import utilities.Pair;

import communication.channel.ChannelElement;

public class SPGraphWorker extends GraphWorker<SPGraphVertex,SPGraphEdge> {
	private static final long serialVersionUID = 1L;

	private Aggregator<Pair<Boolean,Integer>,Boolean> aggregator;

	private boolean finish;

	public SPGraphWorker(int numberWorker, int numberVertexes, int numberWorkers) {
		super(numberWorker, numberVertexes, numberWorkers, 30, TimeUnit.SECONDS);

		this.finish = false;
	}

	protected DefaultDirectedGraph<SPGraphVertex,SPGraphEdge> createGraph() {
		return new DefaultDirectedGraph<SPGraphVertex,SPGraphEdge>(SPGraphEdge.class);
	}

	@SuppressWarnings("unchecked")
	protected boolean performInitialization() {
		loadGraph();

		try {
			this.aggregator = (Aggregator<Pair<Boolean, Integer>, Boolean>) nodeGroup.getManager().obtainAggregator("shortestpath", "finish");
		} catch (RemoteException exception) {
			return false;
		}

		SPGraphVertex vertex = vertexMap.get("0");

		if(vertex != null) {
			updateVertex(vertex, 0);		
		}

		return true;
	}

	protected void performAction(ChannelElement channelElement) {
		if(finish == true) {
			try {
				finish = false;

				aggregator.updateAggregate(new Pair<Boolean,Integer>(false, numberWorker));
			} catch (RemoteException exception) {
				System.err.println("Error obtaining information from the aggregator: " + exception);
			}
		}

		if(channelElement instanceof SPGraphUpdateMessage) {
			SPGraphUpdateMessage message = (SPGraphUpdateMessage) channelElement;

			SPGraphVertex vertex = vertexMap.get(message.getVertexName());

			double distance = message.getDistance();

			updateVertex(vertex, distance);
		}
	}

	private void updateVertex(SPGraphVertex vertex, double distance) {
		if(distance >= vertex.getDistance()) {
			return;
		}

		vertex.setDistance(distance);

		// Recalculate internal neighbor distances

		for(SPGraphEdge edge: graph.outgoingEdgesOf(vertex)) {
			SPGraphVertex neighbor = graph.getEdgeTarget(edge);

			updateVertex(neighbor, vertex.getDistance() + edge.getDistance());
		}

		// Send messages to foreign neighbors
		if(foreignEdges.containsKey(vertex.getName())) {
			for(SPGraphEdge foreignEdge: foreignEdges.get(vertex.getName())) {
				String ownerWorker = obtainOwnerWorker(foreignEdge.getTargetName());

				SPGraphUpdateMessage updateMessage = new SPGraphUpdateMessage(foreignEdge.getTargetName(), vertex.getDistance() + foreignEdge.getDistance());

				write(updateMessage, ownerWorker);
			}
		}
	}

	protected boolean performTermination() {
		dumpGraph();

		return true;
	}

	protected boolean dynamicallyVerifyTermination() {
		if(finish == false) {
			try {
				finish = true;

				aggregator.updateAggregate(new Pair<Boolean,Integer>(true, numberWorker));
			} catch (RemoteException exception) {
				System.err.println("Error updating the aggregator: " + exception);
			}	
		}

		try {
			boolean result = aggregator.obtainAggregate();

			return result;
		} catch (RemoteException exception) {
			System.err.println("Error obtaining information from the aggregator: " + exception);
		}

		return true;
	}

	protected void initiateReaderShufflers() {
		createReaderShuffler(true, false);
	}
}
