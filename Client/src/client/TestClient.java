/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package client;

import java.util.Random;

import java.rmi.RemoteException;

import enums.CommunicationMode;

import exceptions.OverlapingFilesException;

import interfaces.Manager;

import appspecs.ApplicationSpecification;
import appspecs.Node;

import nodes.ReaderSomeoneWriterSomeone;
import nodes.TrivialNode;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Directory;
import utilities.filesystem.Filename;

import utilities.RMIHelper;

public class TestClient {
	private String registryLocation;

	private Directory baseDirectory;

	public TestClient(String registryLocation, Directory baseDirectory) {
		this.registryLocation = registryLocation;

		this.baseDirectory = baseDirectory;
	}

	public void performTest1() {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test1", baseDirectory);

		Filename inputFilename;
		Filename outputFilename;

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		for(int i = 0; i < nodesStage1.length; i++) {
			inputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), "input-stage1-" + i + ".dat", baseDirectory.getProtocol());

			applicationSpecification.addInput(nodesStage1[i], inputFilename);
		}

		for(int i = 0; i < nodesStage1.length; i++) {
			outputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), "output-stage1-" + i + ".dat", baseDirectory.getProtocol());

			try {
				applicationSpecification.addOutput(nodesStage1[i], outputFilename);
			} catch (OverlapingFilesException exception) {
				System.err.println(exception);

				System.exit(1);
			}
		}

		try {
			applicationSpecification.finalize();
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public void performTest2(CommunicationMode communicationMode) {
		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		ApplicationSpecification applicationSpecification = new ApplicationSpecification("test2", baseDirectory);

		Filename inputFilename;
		Filename outputFilename;

		Node[] nodesStage1 = new Node[1];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage1);

		for(int i = 0; i < nodesStage1.length; i++) {
			inputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), "input-stage1-" + i + ".dat", baseDirectory.getProtocol());

			applicationSpecification.addInput(nodesStage1[i], inputFilename);
		}

		Node[] nodesStage2 = new Node[1];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new ReaderSomeoneWriterSomeone();
		}

		applicationSpecification.insertNodes(nodesStage2);

		for(int i = 0; i < nodesStage2.length; i++) {
			outputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), "output-stage2-" + i + ".dat", baseDirectory.getProtocol());

			try {
				applicationSpecification.addOutput(nodesStage2[i], outputFilename);
			} catch (OverlapingFilesException exception) {
				System.err.println(exception);

				System.exit(1);
			}
		}

		applicationSpecification.insertEdges(nodesStage1, nodesStage2, communicationMode, -1);

		try {
			applicationSpecification.finalize();
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

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

		Node[] nodes = new Node[numberNodesEdges];

		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = new TrivialNode();
		}

		applicationSpecification.insertNodes(nodes);

		Filename inputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), "fake-input.dat", baseDirectory.getProtocol());

		applicationSpecification.addInput(nodes[0], inputFilename);

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

					applicationSpecification.insertEdges(source, target, CommunicationMode.TCP);
					numberEdges++;
				}
			}
		}

		System.out.println("Edges created: " + numberEdges);

		try {
			applicationSpecification.finalize();
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		try {
			manager.registerApplication(applicationSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public static void main(String[] arguments) {
		String registryLocation = System.getProperty("java.rmi.server.location");

		String baseDirectory = System.getProperty("hammr.client.basedir"); 

		String command = arguments[0];

		if(command.equals("test1")) {
			TestClient testClient = new TestClient(registryLocation, new Directory(baseDirectory));

			testClient.performTest1();

			System.exit(0);
		}

		if(command.equals("test2")) {
			TestClient testClient = new TestClient(registryLocation, new Directory(baseDirectory));

			testClient.performTest2(CommunicationMode.TCP);

			System.exit(0);
		}

		if(command.equals("test3")) {
			if(arguments.length <= 1) {
				System.err.println("Usage: Client test3 <number_nodes_edges>");

				System.exit(1);
			}

			TestClient testClient = new TestClient(registryLocation, new Directory(baseDirectory));

			int numberNodesEdges = Integer.parseInt(arguments[3]);

			testClient.performTest3(numberNodesEdges);

			System.exit(0);
		}
	}
}