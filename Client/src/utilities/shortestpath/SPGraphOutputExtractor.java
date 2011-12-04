/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.shortestpath;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

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


	public static void main(String[] arguments) {
		if(arguments.length <= 4) {
			System.err.println("usage: SPGraphOutputExtractor baseDirectory [<input> ... <input>]");

			System.exit(1);
		}

		String baseDirectory = arguments[0];

		List<String> inputsList = new ArrayList<String>();

		for(int i = 1; i < arguments.length; i++) {
			inputsList.add(arguments[i]);
		}

		String[] inputsArray = inputsList.toArray(new String[inputsList.size()]);

		SPGraphOutputExtractor extractor = new SPGraphOutputExtractor(new Directory(baseDirectory), inputsArray);

		try {
			extractor.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
