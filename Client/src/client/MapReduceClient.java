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

import exceptions.InexistentInputException;
import exceptions.OverlapingFilesException;

import interfaces.Manager;

import appspecs.Node;

import nodes.ReaderSomeoneWriterSomeone;

import mapreduce.appspecs.MapReduceSpecification;

import mapreduce.programs.counting.CountingMapper;
import mapreduce.programs.counting.CountingMerger;
import mapreduce.programs.counting.CountingReducer;

import utilities.filesystem.FileHelper;

import utilities.filesystem.Directory;
import utilities.filesystem.Filename;

import utilities.RMIHelper;

public class MapReduceClient {
	private String registryLocation;

	private Directory baseDirectory;

	public MapReduceClient(String registryLocation, Directory baseDirectory) {
		this.registryLocation = registryLocation;

		this.baseDirectory = baseDirectory;
	}

	public void run(String[] inputs, boolean useTCP, boolean initialSplit, boolean finalMerge) {
		int numberMappers;
		int numberReducers;

		numberMappers = numberReducers = inputs.length;

		Manager manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		// Create the MapReduce specification

		MapReduceSpecification mapReduceSpecification = new MapReduceSpecification("mapreduce", baseDirectory);

		// Insert the mappers

		Node[] nodesStage1 = new Node[numberMappers];

		for(int i = 0; i < nodesStage1.length; i++) {
			nodesStage1[i] = new CountingMapper<String>(numberReducers);
		}

		try {
			if(initialSplit) {
				mapReduceSpecification.insertMappers(FileHelper.getFileInformation(baseDirectory.getPath(), inputs[0], baseDirectory.getProtocol()), new ReaderSomeoneWriterSomeone(), nodesStage1);
			}
			else {
				// Create the input filenames

				Filename[] inputFilenames = new Filename[numberMappers];

				for(int i = 0; i < inputs.length; i++) {
					inputFilenames[i] = FileHelper.getFileInformation(baseDirectory.getPath(), inputs[i], baseDirectory.getProtocol());
				}

				mapReduceSpecification.insertMappers(inputFilenames, nodesStage1);
			}
		} catch (InexistentInputException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		// Insert the reducers

		Node[] nodesStage2 = new Node[numberReducers];

		for(int i = 0; i < nodesStage2.length; i++) {
			nodesStage2[i] = new CountingReducer<String>();
		}

		try {
			if(finalMerge) {
				mapReduceSpecification.insertReducers(FileHelper.getFileInformation(baseDirectory.getPath(), inputs[0] + ".out", baseDirectory.getProtocol()), new CountingMerger<String>(), nodesStage2);
			}
			else {
				// Append a ".out" extension to the input filenames to form the output filenames

				Filename[] outputFilenames = new Filename[numberReducers];

				for(int i = 0; i < inputs.length; i++) {
					outputFilenames[i] = FileHelper.getFileInformation(baseDirectory.getPath(), inputs[i] + ".out", baseDirectory.getProtocol());
				}

				mapReduceSpecification.insertReducers(outputFilenames, nodesStage2);
			}
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		try {
			mapReduceSpecification.setupCommunication(useTCP);
		} catch (OverlapingFilesException exception) {
			System.err.println(exception);

			System.exit(1);
		}

		try {
			manager.registerApplication(mapReduceSpecification);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");
			exception.printStackTrace();

			System.exit(1);
		}
	}

	public static void main(String[] arguments) {
		String registryLocation = System.getProperty("java.rmi.server.location");

		String baseDirectory = System.getProperty("hammr.client.basedir"); 

		if(arguments.length <= 3) {
			System.err.println("Usage: Client <useTCP> <performSplit> <performMerge> [<input_filename> ... <input_filename>]");
			System.err.println("<useTCP> \"true\" or \"false\""); 
			System.err.println("<performSplit> \"true\" or \"false\"");
			System.err.println("<performMerge> \"true\" or \"false\"");

			System.exit(1);
		}

		MapReduceClient mrClient = new MapReduceClient(registryLocation, new Directory(baseDirectory));

		boolean useTCP = false;

		if(arguments[0].equals("true")) {
			useTCP = true;
		}
		else if(arguments[0].equals("false")) {
			useTCP = false;
		}
		else {
			System.err.println("<useTCP> \"true\" or \"false\""); 

			System.exit(1);
		}

		boolean performMerge = false;

		if(arguments[1].equals("true")) {
			performMerge = true;
		}
		else if(arguments[1].equals("false")) {
			performMerge = false;
		}
		else {
			System.err.println("<performMerge> \"true\" or \"false\"");

			System.exit(1);
		}

		boolean performSplit = false;

		if(arguments[2].equals("true")) {
			performSplit = true;
		}
		else if(arguments[2].equals("false")) {
			performSplit = false;
		}
		else {
			System.err.println("<performSplit> \"true\" or \"false\"");

			System.exit(1);
		}

		List<String> inputsList = new ArrayList<String>();

		for(int i = 3; i < arguments.length; i++) {
			inputsList.add(arguments[i]);
		}

		String[] inputsArray = inputsList.toArray(new String[inputsList.size()]);

		mrClient.run(inputsArray, useTCP, performSplit, performMerge);
	}
}
