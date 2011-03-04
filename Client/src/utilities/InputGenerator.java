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
