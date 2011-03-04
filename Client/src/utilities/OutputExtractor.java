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

	public OutputExtractor(String[] inputOutputPairs) {
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

	public OutputExtractor(String[] inputs, String[] outputs) {
		if(inputs.length != outputs.length) {
			System.err.println("The size of inputs and outputs must be the same");

			throw new IllegalStateException();
		}

		this.inputs = inputs;
		this.outputs = outputs;
	}

	public void run() throws IOException {
		for(int i = 0; i < inputs.length; i++) {
			FileChannelElementReader reader = new FileChannelElementReader(inputs[i]);

			BufferedWriter writer = new BufferedWriter(new FileWriter(outputs[i % outputs.length], true));

			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = reader.read();
				} catch(EOFException exception) {
					break;
				}

				writer.write(obtainInformation(channelElement));
			}

			writer.close();
		}
	}

	protected abstract String obtainInformation(ChannelElement channelElement);
}
