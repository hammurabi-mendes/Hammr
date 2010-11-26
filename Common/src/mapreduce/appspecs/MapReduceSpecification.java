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
