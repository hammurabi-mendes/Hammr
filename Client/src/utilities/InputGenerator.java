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
import java.util.Set;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

import communication.ChannelElement;
import communication.FileChannelElementWriter;

public abstract class InputGenerator {
	private String[] inputs;
	private String[] outputs;

	public InputGenerator(String[] inputsOutputs) {
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

		this.inputs = inputList.toArray(new String[inputList.size()]);
		this.outputs = outputList.toArray(new String[outputList.size()]);
	}

	public InputGenerator(String[] inputs, String[] outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public void run() throws IOException {
		BufferedReader[] readers = new BufferedReader[inputs.length];
		
		for(int i = 0; i < inputs.length; i++) {
			readers[i] = new BufferedReader(new FileReader(inputs[i]));
		}
		
		FileChannelElementWriter[] writers = new FileChannelElementWriter[outputs.length];
		
		for(int i = 0; i < outputs.length; i++) {
			writers[i] =  new FileChannelElementWriter(outputs[i]);
		}
		
		int writerCount = 0;
		
		for(int i = 0; i < readers.length; i++) {
			String buffer;

			while(true) {
				buffer = obtainBuffer(readers[i]);

				if(buffer == null) {
					break;
				}

				Set<ChannelElement> channelElements = generateInput(buffer);

				for(ChannelElement channelElement: channelElements) {
					writers[(writerCount++) % writers.length].write(channelElement);
				}
			}
		}

		for(int i = 0; i < readers.length; i++) {
			readers[i].close();
		}
		
		for(int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
	}

	protected abstract String obtainBuffer(BufferedReader reader) throws IOException;

	protected abstract Set<ChannelElement> generateInput(String buffer);
}
