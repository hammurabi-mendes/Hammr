/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package client;

import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

import exceptions.OverlapingFilesException;

import interfaces.Manager;

import appspecs.Node;

import graphs.appspecs.GraphProcessingSpecification;

import graphs.programs.shortestpath.SPGraphWorker;

import graphs.programs.shortestpath.SPFinishController;

import graphs.programs.shortestpath.SPGraphEdge;
import graphs.programs.shortestpath.SPGraphVertex;

import enums.CommunicationMode;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Filename;
import utilities.filesystem.Directory;

import utilities.RMIHelper;

public class ShortestPathClient {
	private String registryLocation;

	private Directory baseDirectory;

	public ShortestPathClient(String registryLocation, Directory baseDirectory) {
		this.registryLocation = registryLocation;

		this.baseDirectory = baseDirectory;
	}

	public void run(String[] inputs, int numberVertexes) {
		int numberWorkers = inputs.length;

		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		GraphProcessingSpecification<SPGraphVertex,SPGraphEdge> graphProcessingSpecification = new GraphProcessingSpecification<SPGraphVertex,SPGraphEdge>("shortestpath", baseDirectory);

		// Add the workers

		Node[] workers = new Node[numberWorkers];

		for(int i = 0; i < workers.length; i++) {
			workers[i] = new SPGraphWorker(i, numberVertexes, numberWorkers);
		}

		graphProcessingSpecification.insertWorkers(workers);

		// Add the inputs and outputs

		Filename inputFilename;
		Filename outputFilename;

		try {
			for(int i = 0; i < workers.length; i++) {
				inputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), inputs[i], baseDirectory.getProtocol());
				outputFilename = FileHelper.getFileInformation(baseDirectory.getPath(), inputs[i] + ".out", baseDirectory.getProtocol());

				graphProcessingSpecification.addInput(workers[i], inputFilename);
				graphProcessingSpecification.addOutput(workers[i], outputFilename);
			}
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		// Add pairwise TCP communication among the workers

		graphProcessingSpecification.insertEdges(workers, workers, CommunicationMode.TCP);

		try {
			graphProcessingSpecification.finalize();
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		// Add a controller to permit workers detect the end of the iterative processes

		graphProcessingSpecification.addController("finish", new SPFinishController("finish", numberWorkers));

		try {
			manager.registerApplication(graphProcessingSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");

			System.exit(1);
		}
	}

	public static void main(String[] arguments) {
		String registryLocation = System.getProperty("java.rmi.server.location");

		String baseDirectory = System.getProperty("hammr.client.basedir"); 

		if(arguments.length <= 1) {
			System.err.println("Usage: Client <number_vertexes> [<input_filename> ... <input_filename>]");

			System.exit(1);
		}

		int numberVertexes = Integer.valueOf(arguments[0]);

		List<String> inputsList = new ArrayList<String>();

		for(int i = 1; i < arguments.length; i++) {
			inputsList.add(arguments[i]);
		}

		String[] inputsArray = inputsList.toArray(new String[inputsList.size()]);

		ShortestPathClient shortestPathClient = new ShortestPathClient(registryLocation, new Directory(baseDirectory));

		shortestPathClient.run(inputsArray, numberVertexes);
	}
}
