/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package mapreduce.appspecs;

import appspecs.ApplicationSpecification;
import appspecs.Node;
import appspecs.EdgeType;

import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;

public class MapReduceSpecification extends ApplicationSpecification {
	private static final long serialVersionUID = 1L;

	private Node[] splitStage;

	private Node[] mapStage;
	private Node[] reduceStage;

	private Node[] mergeStage;

	public MapReduceSpecification(String name, String directoryPrefix) {
		super(name, directoryPrefix);
	}

	public void insertMappers(String input, Node splitter, Node[] mappers) throws InexistentInputException {
		stageSplitter(splitter);

		addInitial(splitter, input);

		stageMappers(mappers);

		insertEdges(splitStage, mapStage, EdgeType.FILE);
	}

	public void insertMappers(String[] inputs, Node[] mappers) throws InexistentInputException {
		stageMappers(mappers);

		for(int i = 0; i < inputs.length; i++) {
			addInitial(mappers[i], inputs[i]);
		}
	}

	public void insertReducers(String output, Node merger, Node[] reducers) throws OverlappingOutputException {
		stageReducers(reducers);

		stageMerger(merger);

		addFinal(mergeStage[0], output);

		insertEdges(reduceStage, mergeStage, EdgeType.FILE);
	}

	public void insertReducers(String[] outputs, Node[] reducers) throws OverlappingOutputException {
		stageReducers(reducers);

		for(int i = 0; i < outputs.length; i++) {
			addFinal(reducers[i], outputs[i]);
		}
	}

	public void setupCommunication(Type type) {
		if(type == Type.TCPBASED) {
			insertEdges(mapStage, reduceStage, EdgeType.TCP);
		}
		else if(type == Type.FILEBASED) {
			insertEdges(mapStage, reduceStage, EdgeType.FILE);
		}

		finalize();
	}

	private void stageSplitter(Node splitter) {
		nameGenerationString = "splitter-";
		nameGenerationCounter = 0L;

		splitStage = new Node[1];

		splitStage[0] = splitter;

		insertNodes(splitStage);
	}

	private void stageMerger(Node merger) {
		nameGenerationString = "merger-";
		nameGenerationCounter = 0L;

		mergeStage = new Node[1];

		mergeStage[0] = merger;

		insertNodes(mergeStage);
	}

	private void stageMappers(Node[] mappers) {
		mapStage = mappers;

		setupMapperNaming();
		insertNodes(mapStage);
	}

	private void stageReducers(Node[] reducers) {
		reduceStage = reducers;

		setupReducerNaming();
		insertNodes(reduceStage);
	}

	private void setupMapperNaming() {
		nameGenerationString = "mapper-";
		nameGenerationCounter = 0L;
	}

	private void setupReducerNaming() {
		nameGenerationString = "reducer-";
		nameGenerationCounter = 0L;
	}

	public enum Type {
		TCPBASED, FILEBASED;
	}
}
