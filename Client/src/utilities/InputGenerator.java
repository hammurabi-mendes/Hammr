package utilities;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

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

	protected String obtainBuffer(BufferedReader reader) throws IOException {
		return reader.readLine();
	}

	protected Set<ChannelElement> generateInput(String buffer) {
		Set<ChannelElement> result = new HashSet<ChannelElement>();

		result.add(new ChannelElement(buffer, null));

		return result;
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
