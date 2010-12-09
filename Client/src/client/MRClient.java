package client;

import java.util.Set;
import java.util.HashSet;

import java.rmi.RemoteException;

import appspecs.ApplicationSpecification;

import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.programs.counting.CountingMapper;
import mapreduce.programs.counting.CountingReducer;
import mapreduce.programs.counting.CountingMerger;

import interfaces.Manager;

import appspecs.Node;

import programs.ReaderSomeoneWriterSomeone;

import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;

import utilities.RMIHelper;

public class MRClient {
	private String registryLocation;

	public MRClient(String registryLocation) {
		this.registryLocation = registryLocation;
	}

	public Set<String> split(String inputFilename, int numberInputs) {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new MapReduceSpecification("mapreduce", "/Users/hmendes/brown/DC/Project");

		if(!applicationSpecification.initialize()) {
			System.err.println("The directory " + applicationSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		try {
			applicationSpecification.addInitial(nodesStage1[0], inputFilename);
		} catch (InexistentInputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		Set<String> result = new HashSet<String>();

		for(int i = 0; i < numberInputs; i++) {
			String outputFilename = "datasplit-" + i + ".dat";

			try {
				applicationSpecification.addFinal(nodesStage1[0], outputFilename);
			} catch (OverlappingOutputException exception) {
				System.err.println(exception);

				System.exit(1);
			}

			result.add(outputFilename);
		}

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}

		return result;
	}

	public void performMapReduce(String[] inputFilenames, MapReduceSpecification.Type edgeType) {
		int numberMappers;
		int numberReducers;

		numberMappers = numberReducers = inputFilenames.length;

		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		MapReduceSpecification mapReduceSpecification = new MapReduceSpecification("mapreduce", "/Users/hmendes/brown/DC/Project");

		if(!mapReduceSpecification.initialize()) {
			System.err.println("The directory " + mapReduceSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		Node[] nodesStage1 = new Node[numberMappers];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new CountingMapper<String>(numberReducers);
		}

		try {
			mapReduceSpecification.insertMappers(inputFilenames, nodesStage1);
		} catch (InexistentInputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		Node[] nodesStage2 = new Node[numberReducers];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new CountingReducer<String>();
		}

		try {
			mapReduceSpecification.insertReducers("output-merger.dat", new CountingMerger<String>(), nodesStage2);
		} catch (OverlappingOutputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		mapReduceSpecification.setupCommunication(edgeType);

		try {
			manager.registerApplication(mapReduceSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}
}
