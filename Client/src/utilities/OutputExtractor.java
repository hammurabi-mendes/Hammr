package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.EOFException;
import java.io.IOException;

import communication.ChannelElement;
import communication.FileChannelElementReader;

public class OutputExtractor {
	private String input;
	private String output;

	public OutputExtractor(String input, String output) {
		this.input = input;
		this.output = output;
	}

	public void run() throws IOException {
		FileChannelElementReader reader = new FileChannelElementReader(input);

		BufferedWriter writer = new BufferedWriter(new FileWriter(output));

		ChannelElement channelElement;

		while(true) {
			try {
				channelElement = reader.read();
			} catch(EOFException exception) {
				break;
			}

			writer.write(generateOutput(channelElement));
		}

		writer.close();
	}

	protected String generateOutput(ChannelElement channelElement) {
		return ((String) channelElement.getObject()) + "\n";
	}

	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Please provide an input and an output filename.");

			System.exit(1);
		}

		OutputExtractor extractor = new OutputExtractor(arguments[0], arguments[1]);

		try {
			extractor.run();
		} catch (IOException exception) {
			System.err.println("Error extracting output");

			exception.printStackTrace();
		}
	}
}
