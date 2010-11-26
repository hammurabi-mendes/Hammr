package utilities.counting;

import java.io.IOException;

import utilities.InputGenerator;

import communication.ChannelElement;
import mapreduce.communication.MRChannelElement;

public class CountingInputGenerator extends InputGenerator {
	public CountingInputGenerator(String input, String output) {
		super(input, output);
	}

	protected ChannelElement generateInput(String line) {
		return new MRChannelElement<String,Long>(line, 0L);
	}

	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Please provide an input and an output filename.");

			System.exit(1);
		}

		CountingInputGenerator generator = new CountingInputGenerator(arguments[0], arguments[1]);

		try {
			generator.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
