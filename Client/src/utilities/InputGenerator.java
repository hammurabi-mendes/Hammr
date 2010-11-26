package utilities;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

import communication.ChannelElement;

import communication.FileChannelElementWriter;

public class InputGenerator {
	private String input;
	private String output;

	public InputGenerator(String input, String output) {
		this.input = input;
		this.output = output;
	}

	public void run() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));

		FileChannelElementWriter writer = new FileChannelElementWriter(output);

		String line;

		while(true) {
			line = reader.readLine();

			if(line == null) {
				break;
			}

			writer.write(generateInput(line));
		}

		writer.close();
	}

	protected ChannelElement generateInput(String line) {
		return new ChannelElement(line, null);
	}

	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Please provide an input and an output filename.");

			System.exit(1);
		}

		InputGenerator generator = new InputGenerator(arguments[0], arguments[1]);

		try {
			generator.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
