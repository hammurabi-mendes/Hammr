package client;

import java.rmi.RemoteException;

import mapreduce.appspecs.MapReduceSpecification;
import mapreduce.programs.counting.CountingMapper;
import mapreduce.programs.counting.CountingReducer;

import interfaces.Manager;

import appspecs.ApplicationSpecification;
import appspecs.EdgeType;
import appspecs.Node;

import programs.ReaderSomeoneWriterSomeone;
import programs.ReaderSomeoneWriterEveryone;

import appspecs.exceptions.InexistentInputException;
import appspecs.exceptions.OverlappingOutputException;


import utilities.RMIHelper;

public class Client {
	private String registryLocation;

	public Client(String registryLocation) {
		this.registryLocation = registryLocation;
	}

	public void performTest1() {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test1", "/Users/hmendes/brown/DC/Project");

		if(!applicationSpecification.initialize()) {
			System.err.println("The directory " + applicationSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		String inputFilename;
		String outputFilename;

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		for(int i = 0; i < nodesStage1.length; i++) {
			inputFilename = "input-stage1-" + i + ".dat";

			try {
				applicationSpecification.addInitial(nodesStage1[i], inputFilename);
			} catch (InexistentInputException exception) {
				System.err.println(exception);

				System.exit(1);
			}
		}

		for(int i = 0; i < nodesStage1.length; i++) {
			outputFilename = "output-stage1-" + i + ".dat";

			try {
				applicationSpecification.addFinal(nodesStage1[i], outputFilename);
			} catch (OverlappingOutputException exception) {
				System.err.println(exception);

				System.exit(1);
			}
		}

		applicationSpecification.finalize();

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public void performTest2(EdgeType edgeType) {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test2", "/Users/hmendes/brown/DC/Project");

		if(!applicationSpecification.initialize()) {
			System.err.println("The directory " + applicationSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		String inputFilename;
		String outputFilename;

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		for(int i = 0; i < nodesStage1.length; i++) {
			inputFilename = "input-stage1-" + i + ".dat";

			try {
				applicationSpecification.addInitial(nodesStage1[i], inputFilename);
			} catch (InexistentInputException exception) {
				System.err.println(exception);

				System.exit(1);
			}
			//System.exit(1);
		}

		Node[] nodesStage2 = new Node[1];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage2);

		for(int i = 0; i < nodesStage2.length; i++) {
			outputFilename = "output-stage2-" + i + ".dat";

			try {
				applicationSpecification.addFinal(nodesStage2[i], outputFilename);
			} catch (OverlappingOutputException exception) {
				System.err.println(exception);

				System.exit(1);
			}
		}

		// Change here
		applicationSpecification.insertEdges(nodesStage1, nodesStage2, edgeType, -1);

		applicationSpecification.finalize();

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public void performTest3() {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test3", "/Users/hmendes/brown/DC/Project");

		if(!applicationSpecification.initialize()) {
			System.err.println("The directory " + applicationSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		String inputFilename;

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		for(int i = 0; i < nodesStage1.length; i++) {
			inputFilename = "input-stage1-" + i + ".dat";

			try {
				applicationSpecification.addInitial(nodesStage1[i], inputFilename);
			} catch (InexistentInputException exception) {
				System.err.println(exception);

				System.exit(1);
			}
			//System.exit(1);
		}

		Node[] nodesStage2 = new Node[2];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage2);

		Node[] nodesStage3 = new Node[2];

		for(int i = 0; i < nodesStage3.length; i++) {
			nodesStage3[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage3);

		Node[] nodesStage4 = new Node[4];

		for(int i = 0; i < nodesStage4.length; i++) {
			nodesStage4[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage4);

		Node[] nodesStage5 = new Node[1];

		nodesStage5[0] = new ReaderSomeoneWriterSomeone();

		applicationSpecification.insertNodes(nodesStage5);

		try {
			applicationSpecification.addFinal(nodesStage5[0], "output-stage5-0.dat");
		} catch (OverlappingOutputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		applicationSpecification.insertEdges(nodesStage1, nodesStage2, EdgeType.TCP);
		applicationSpecification.insertEdges(nodesStage2, nodesStage3, EdgeType.TCP, 1);
		applicationSpecification.insertEdges(nodesStage3, nodesStage4, EdgeType.TCP);
		applicationSpecification.insertEdges(nodesStage4, nodesStage5, EdgeType.TCP);

		applicationSpecification.finalize();

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public void performTest4(int numberMappers, int numberReducers) {
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
			mapReduceSpecification.insertMappers("input-splitter.dat", new ReaderSomeoneWriterSomeone(), nodesStage1);
		} catch (InexistentInputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		Node[] nodesStage2 = new Node[numberReducers];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new CountingReducer<String>();
		}

		try {
			mapReduceSpecification.insertReducers("output-merger.dat", new ReaderSomeoneWriterEveryone(), nodesStage2);
		} catch (OverlappingOutputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		mapReduceSpecification.setupCommunication(MapReduceSpecification.Type.TCPBASED);

		try {
			manager.registerApplication(mapReduceSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public static void main(String[] arguments) {
		if(arguments.length != 1) {
			System.err.println("Usage: You must supply the registry location");

			System.exit(1);
		}

		String registryLocation = arguments[0];

		Client client = new Client(registryLocation);

		client.performTest4(5, 5);
	}
}
