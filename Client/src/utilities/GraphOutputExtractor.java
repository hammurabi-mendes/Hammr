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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.io.EOFException;
import java.io.IOException;

import org.jgrapht.graph.DefaultDirectedGraph;

import communication.channel.ChannelElement;

import graphs.communication.EdgeChannelElement;
import graphs.communication.VertexChannelElement;

import communication.readers.FileChannelElementReader;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Filename;
import utilities.filesystem.Directory;

public abstract class GraphOutputExtractor<V extends GraphVertex,E extends GraphEdge> {
	protected DefaultDirectedGraph<V,E> graph;

	protected Map<String,V> vertexMap;

	private Filename[] inputs;

	public GraphOutputExtractor(Directory directory, String[] inputs) {
		List<Filename> inputList = new ArrayList<Filename>();

		for(int i = 0; i < inputs.length; i++) {
			inputList.add(FileHelper.getFileInformation(directory.getPath(), inputs[i], directory.getProtocol()));
		}

		this.inputs = inputList.toArray(new Filename[inputList.size()]);

		this.vertexMap = new HashMap<String,V>();
	}

	@SuppressWarnings("unchecked")
	public void run() throws IOException {
		FileChannelElementReader[] readers = new FileChannelElementReader[inputs.length];

		// First pass: add vertexes

		for(int i = 0; i < inputs.length; i++) {
			readers[i] = new FileChannelElementReader(inputs[i]);
		}

		for(int i = 0; i < readers.length; i++) {
			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = readers[i].read();
				} catch(EOFException exception) {
					break;
				}

				if(channelElement instanceof VertexChannelElement) {
					addVertex(((VertexChannelElement<V>) channelElement).getObject());
				}
			}
		}

		for(int i = 0; i < readers.length; i++) {
			readers[i].close();
		}

		// Second pass: add edges

		for(int i = 0; i < inputs.length; i++) {
			readers[i] = new FileChannelElementReader(inputs[i]);
		}

		for(int i = 0; i < readers.length; i++) {
			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = readers[i].read();
				} catch(EOFException exception) {
					break;
				}

				if(channelElement instanceof EdgeChannelElement) {
					addEdge(((EdgeChannelElement<E>) channelElement).getObject());
				}
			}
		}

		for(int i = 0; i < readers.length; i++) {
			readers[i].close();
		}

	}

	protected abstract void createGraph();

	protected abstract void printGraph();

	protected void addVertex(V vertex) {
		graph.addVertex(vertex);

		vertexMap.put(vertex.getName(), vertex);
	}

	protected void addEdge(E edge) {
		graph.addEdge(vertexMap.get(edge.getSourceName()), vertexMap.get(edge.getTargetName()), edge);
	}
}
