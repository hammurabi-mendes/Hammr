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

	public InputGenerator(String[] inputOutputPairs) {
		if((inputOutputPairs.length % 2) != 0) {
			System.err.println("Parameters: [(<input> <output>) ... (<input> <output>)]");

			System.exit(1);
		}

		List<String> inputList = new ArrayList<String>();
		List<String> outputList = new ArrayList<String>();

		for(int i = 0; i < inputOutputPairs.length; i++) {
			if((i % 2) == 0) {
				inputList.add(inputOutputPairs[i]);
			}
			else {
				outputList.add(inputOutputPairs[i]);
			}
		}	

		this.inputs = inputList.toArray(new String[inputList.size()]);
		this.outputs = outputList.toArray(new String[outputList.size()]);
	}

	public InputGenerator(String[] inputs, String[] outputs) {
		if(inputs.length != outputs.length) {
			System.err.println("The size of inputs and outputs must be the same");

			throw new IllegalStateException();
		}

		this.inputs = inputs;
		this.outputs = outputs;
	}

	public void run() throws IOException {
		for(int i = 0; i < outputs.length; i++) {
			BufferedReader reader = new BufferedReader(new FileReader(inputs[i % inputs.length]));

			FileChannelElementWriter writer = new FileChannelElementWriter(outputs[i]);

			String buffer;

			while(true) {
				buffer = obtainBuffer(reader);

				if(buffer == null) {
					break;
				}

				Set<ChannelElement> channelElements = generateInput(buffer);

				for(ChannelElement channelElement: channelElements) {
					writer.write(channelElement);
				}
			}

			writer.close();
		}
	}

	protected abstract String obtainBuffer(BufferedReader reader) throws IOException;

	protected abstract Set<ChannelElement> generateInput(String buffer);
}
