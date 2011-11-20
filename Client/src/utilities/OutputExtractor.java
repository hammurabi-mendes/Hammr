/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package utilities;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.EOFException;
import java.io.IOException;

import communication.ChannelElement;
import communication.FileChannelElementReader;

public abstract class OutputExtractor {
	private String[] inputs;
	private String[] outputs;

	public OutputExtractor(String[] inputsOutputs) {
		List<String> inputList = new ArrayList<String>();
		List<String> outputList = new ArrayList<String>();

		boolean foundColum = false;

		for(int i = 0; i < inputsOutputs.length; i++) {
			if(inputsOutputs[i].equals(":")) {
				foundColum = true;

				continue;
			}

			if(!foundColum) {
				inputList.add(inputsOutputs[i]);
			}
			else {
				outputList.add(inputsOutputs[i]);
			}
		}	

		if(inputList.size() == 0 || outputList.size() == 0) {
			System.err.println("Parameters: <input> ... <input> : <output> ... <output>");

			System.exit(1);
		}

		if(inputList.size() != outputList.size()) {
			System.err.println("The specified inputs and outputs should have the same size");

			System.exit(1);
		}

		this.inputs = inputList.toArray(new String[inputList.size()]);
		this.outputs = outputList.toArray(new String[outputList.size()]);
	}

	public OutputExtractor(String[] inputs, String[] outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public void run() throws IOException {
		FileChannelElementReader[] readers = new FileChannelElementReader[inputs.length];

		for(int i = 0; i < inputs.length; i++) {
			readers[i] = new FileChannelElementReader(inputs[i]);
		}

		BufferedWriter[] writers = new BufferedWriter[outputs.length];

		for(int i = 0; i < outputs.length; i++) {
			writers[i] = new BufferedWriter(new FileWriter(outputs[i], true));
		}

		for(int i = 0; i < readers.length; i++) {
			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = readers[i].read();
				} catch(EOFException exception) {
					break;
				}

				writers[i].write(obtainInformation(channelElement));
			}
		}

		for(int i = 0; i < readers.length; i++) {
			readers[i].close();
		}

		for(int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
	}

	protected abstract String obtainInformation(ChannelElement channelElement);
}
