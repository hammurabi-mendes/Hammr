/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
