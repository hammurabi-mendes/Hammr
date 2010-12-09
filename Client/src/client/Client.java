package client;

import java.util.Set;

import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

import mapreduce.appspecs.MapReduceSpecification;

import interfaces.Manager;

import appspecs.ApplicationSpecification;
import appspecs.EdgeType;
import appspecs.Node;

import programs.ReaderSomeoneWriterSomeone;

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

	public static void main(String[] arguments) {
		if(arguments.length <= 1) {
			System.err.println("Usage: Client <registry_location> <command> [<command_argument> ... <comand_argument>]");

			System.exit(1);
		}

		String registryLocation = arguments[0];

		MRClient client = new MRClient(registryLocation);

		String command = arguments[1];

		if(command.equals("split_input")) {
			if(arguments.length <= 3) {
				System.err.println("Usage: Client <registry_location> split_input <input_filename> <number_inputs>]");

				System.exit(1);
			}

			String inputFilename = arguments[2];

			int numberInputs = Integer.parseInt(arguments[3]);

			Set<String> filenames = client.split(inputFilename, numberInputs);

			for(String filename: filenames) {
				System.out.println(filename);
			}
		}

		if(command.equals("perform_mapreduce")) {
			if(arguments.length <= 2) {
				System.err.println("Usage: Client <registry_location> perform_mapreduce [<input_filename> ... <input_filename>]");

				System.exit(1);
			}

			List<String> inputFilenames = new ArrayList<String>();

			for(int i = 2; i < arguments.length; i++) {
				inputFilenames.add(arguments[i]);
			}

			String[] finalInputFilenames = inputFilenames.toArray(new String[inputFilenames.size()]);

			client.performMapReduce(finalInputFilenames, MapReduceSpecification.Type.TCPBASED);
		}
	}
}
