package mapreduce.appspecs;

import utilities.DistributedFileSystemFactory;
import mapreduce.programs.Mapper;
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

	public MapReduceSpecification(String name, String userPoolName, String directoryPrefix) {
		super(name, userPoolName, directoryPrefix);
	}

	public void insertMappers(String input, Node splitter, Node[] mappers) throws InexistentInputException {
		stageSplitter(splitter);

		addInitial(splitter, input);

		stageMappers(mappers);

		insertEdges(splitStage, mapStage, EdgeType.FILE);
	}

	public void insertMappers(String[] inputs, Node[] mappers) throws InexistentInputException {
		stageMappers(mappers);
		try {
			int i = 0;
			for (String input : inputs) {
				long length = DistributedFileSystemFactory.getDistributedFileSystem().getFileLength(
						getAbsoluteFileName(input));
				long blocksize = DistributedFileSystemFactory.getDistributedFileSystem().getBlockSize(
						getAbsoluteFileName(input));
				for (long offset = 0; offset < length; offset += blocksize) {
					addInitial(mappers[i++], input, offset, Math.min(offset + blocksize, length));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertMappers(String input, Node[] mappers) {
		try {
			long length = DistributedFileSystemFactory.getDistributedFileSystem().getFileLength(
					getAbsoluteFileName(input));
			long blocksize = DistributedFileSystemFactory.getDistributedFileSystem().getBlockSize(
					getAbsoluteFileName(input));

			int nMappers = mappers.length;
			stageMappers(mappers);
			for (int i = 0; i < nMappers; ++i) {
				addInitial(mappers[i], input, i * blocksize, Math.min((i + 1) * blocksize, length));
			}
		} catch (Exception e) {
			e.printStackTrace();
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

		for (int i = 0; i < outputs.length; i++) {
			addFinal(reducers[i], outputs[i]);
		}
	}

	public void setupCommunication(CommunicationType type) {
		if (type == CommunicationType.TCPBASED) {
			insertEdges(mapStage, reduceStage, EdgeType.TCP);
		} else if (type == CommunicationType.FILEBASED) {
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
		nameGenerationString = getName() + "-mapper-";
		nameGenerationCounter = 0L;
	}

	private void setupReducerNaming() {
		nameGenerationString = getName() + "-reducer-";
		nameGenerationCounter = 0L;
	}

	public enum CommunicationType {
		TCPBASED, FILEBASED;
	}
}
