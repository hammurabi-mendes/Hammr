package client;

import java.rmi.RemoteException;

import mapreduce.appspecs.MapReduceSpecification;

import mapreduce.programs.counting.CountingMapper;
import mapreduce.programs.counting.CountingReducer;
import mapreduce.programs.counting.CountingMerger;

import interfaces.Manager;

import appspecs.Node;

import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;

import utilities.RMIHelper;

public class MRClient {
	private String registryLocation;

	private String baseDirectory;

	public MRClient(String registryLocation, String baseDirectory) {
		this.registryLocation = registryLocation;

		this.baseDirectory = baseDirectory;
	}

	public void performMapReduce(String[] inputFilenames, MapReduceSpecification.Type edgeType, boolean finalMerge) {
		int numberMappers;
		int numberReducers;

		numberMappers = numberReducers = inputFilenames.length;

		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		MapReduceSpecification mapReduceSpecification = new MapReduceSpecification("mapreduce", baseDirectory);

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
			if(finalMerge) {
				mapReduceSpecification.insertReducers("output-merger.dat", new CountingMerger<String>(), nodesStage2);
			}
			else {
				// Append a ".out" extension to the input filenames to form the output filenames

				String[] outputFilenames = new String[numberReducers];

				for(int i = 0; i < inputFilenames.length; i++) {
					outputFilenames[i] = inputFilenames[i] + ".out";
				}

				mapReduceSpecification.insertReducers(outputFilenames, nodesStage2);
			}
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
