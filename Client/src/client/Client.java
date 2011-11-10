/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
			if(arguments.length <= 5) {
				System.err.println("Usage: Client <registry_location> <base_directory> perform_mapreduce <type> <join> [<input_filename> ... <input_filename>]");
				System.err.println("<type>: \"TCP\" or \"FILE\"");
				System.err.println("<join> \"true\" or \"false\"");

				System.exit(1);
			}

			MRClient client = new MRClient(registryLocation, baseDirectory);

			MapReduceSpecification.Type type = MapReduceSpecification.Type.FILEBASED;

			if(arguments[3].equals("TCP")) {
				type = MapReduceSpecification.Type.TCPBASED;
			}
			else if(arguments[3].equals("FILE")) {
				type = MapReduceSpecification.Type.FILEBASED;
			}
			else {
				System.err.println("<type>: \"TCP\" or \"FILE\"");

				System.exit(1);
			}

			boolean join = false;

			if(arguments[4].equals("true")) {
				join = true;
			}
			else if(arguments[4].equals("false")) {
				join = false;
			}
			else {
				System.err.println("<join> \"true\" or \"false\"");

				System.exit(1);
			}

			List<String> temporaryInputFilenames = new ArrayList<String>();

			for(int i = 5; i < arguments.length; i++) {
				temporaryInputFilenames.add(arguments[i]);
			}

			String[] finalInputFilenames = temporaryInputFilenames.toArray(new String[temporaryInputFilenames.size()]);

			client.performMapReduce(finalInputFilenames, type, join);
		}
	}
}
