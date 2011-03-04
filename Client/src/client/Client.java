package client;

import java.util.Random;

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

	private String baseDirectory;

	public Client(String registryLocation, String baseDirectory) {
		this.registryLocation = registryLocation;

		this.baseDirectory = baseDirectory;
	}

	public void performTest1() {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test1", baseDirectory);

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

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test2", baseDirectory);

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

	public void performTest3(int numberNodesEdges) {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test3", baseDirectory);

		if(!applicationSpecification.initialize()) {
			System.err.println("The directory " + applicationSpecification.getAbsoluteDirectory() + " does not exist");

			System.exit(1);
		}

		Node[] nodes = new Node[numberNodesEdges];

		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = new TrivialNode();
		}

		applicationSpecification.insertNodes(nodes);

		String inputFilename = "fake-input.dat";

		try {
			applicationSpecification.addInitial(nodes[0], inputFilename);
		} catch (InexistentInputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		Random random = new Random();

		int dice;

		Node[] source = new Node[1];
		Node[] target = new Node[1];

		int numberEdges = 0;

		for(int i = 0; i < nodes.length; i++) {
			for(int j = i + 1; j < nodes.length; j++) {
				dice = random.nextInt() % 1000;

				if(dice <= 0) {
					source[0] = nodes[i];
					target[0] = nodes[j];

					applicationSpecification.insertEdges(source, target, EdgeType.TCP);
					numberEdges++;
				}
			}
		}

		System.out.println("Edges created: " + numberEdges);

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
		if(arguments.length <= 2) {
			System.err.println("Usage: Client <registry_location> <base_directory> <command> [<command_argument> ... <comand_argument>]");

			System.exit(1);
		}

		String registryLocation = arguments[0];

		String baseDirectory = arguments[1];

		String command = arguments[2];

		if(command.equals("test1")) {
			Client client = new Client(registryLocation, baseDirectory);

			client.performTest1();

			System.exit(0);
		}

		if(command.equals("test2")) {
			Client client = new Client(registryLocation, baseDirectory);

			client.performTest2(EdgeType.TCP);

			System.exit(0);
		}

		if(command.equals("test3")) {
			if(arguments.length <= 3) {
				System.err.println("Usage: Client <registry_location> <base_directory> test3 <number_nodes_edges>");

				System.exit(1);
			}

			Client client = new Client(registryLocation, baseDirectory);

			int numberNodesEdges = Integer.parseInt(arguments[3]);

			client.performTest3(numberNodesEdges);

			System.exit(0);
		}

		if(command.equals("perform_mapreduce")) {
			if(arguments.length <= 4) {
				System.err.println("Usage: Client <registry_location> <base_directory> perform_mapreduce <type> [<input_filename> ... <input_filename>]");

				System.exit(1);
			}

			MRClient client = new MRClient(registryLocation, baseDirectory);

			MapReduceSpecification.Type type = (arguments[3].equals("TCP") ? MapReduceSpecification.Type.TCPBASED : MapReduceSpecification.Type.FILEBASED);

			List<String> temporaryInputFilenames = new ArrayList<String>();

			for(int i = 4; i < arguments.length; i++) {
				temporaryInputFilenames.add(arguments[i]);
			}

			String[] finalInputFilenames = temporaryInputFilenames.toArray(new String[temporaryInputFilenames.size()]);

			client.performMapReduce(finalInputFilenames, type);
		}
	}
}
