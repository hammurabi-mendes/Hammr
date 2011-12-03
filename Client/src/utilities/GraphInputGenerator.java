/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities;

import graphs.programs.GraphVertex;
import graphs.programs.GraphEdge;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import org.jgrapht.graph.AbstractBaseGraph;

import org.jgrapht.traverse.BreadthFirstIterator;

import graphs.communication.EdgeChannelElement;
import graphs.communication.VertexChannelElement;

import communication.writers.FileChannelElementWriter;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Filename;
import utilities.filesystem.Directory;

public abstract class GraphInputGenerator<V extends GraphVertex,E extends GraphEdge> {
	protected AbstractBaseGraph<V,E> graph;

	protected Filename[] outputs;

	public GraphInputGenerator(Directory directory, String[] outputs) {
		obtainGraph();

		List<Filename> outputList = new ArrayList<Filename>();

		for(int i = 0; i < outputs.length; i++) {
			outputList.add(FileHelper.getFileInformation(directory.getPath(), outputs[i], directory.getProtocol()));
		}

		this.outputs = outputList.toArray(new Filename[outputList.size()]);
	}

	protected abstract void obtainGraph();

	public void run() throws IOException {
		// First, give name to all vertices and edges of the graph

		BreadthFirstIterator<V,E> iterator1 = new BreadthFirstIterator<V,E>(graph);

		int index = 0;

		while(iterator1.hasNext()) {
			V vertex = iterator1.next();

			vertex.setName(String.valueOf(index++));
		}

		for(E edge: graph.edgeSet()) {
			V source = graph.getEdgeSource(edge);
			V target = graph.getEdgeSource(edge);

			edge.setName(source.getName() + "," + target.getName());

			edge.setSourceName(source.getName());
			edge.setTargetName(target.getName());
		}

		FileChannelElementWriter[] writers = new FileChannelElementWriter[outputs.length];

		for(int i = 0; i < outputs.length; i++) {
			writers[i] =  new FileChannelElementWriter(outputs[i]);
		}

		int shareWriters = (graph.vertexSet().size() / writers.length);
		int indexWrite = 0;

		BreadthFirstIterator<V,E> iterator2 = new BreadthFirstIterator<V,E>(graph);

		while(iterator2.hasNext()) {
			V vertex = iterator2.next();

			writers[indexWrite / shareWriters].write(new VertexChannelElement<V>(vertex));

			for(E edge: graph.outgoingEdgesOf(vertex)) {
				writers[indexWrite / shareWriters].write(new EdgeChannelElement<E>(edge));
			}

			indexWrite++;
		}

		for(int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
	}
}