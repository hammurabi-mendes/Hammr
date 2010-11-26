package utilities.counting;

import java.io.IOException;

import utilities.OutputExtractor;

import communication.ChannelElement;
import mapreduce.communication.MRChannelElement;

public class CountingOutputExtractor extends OutputExtractor {
	public CountingOutputExtractor(String input, String output) {
		super(input, output);
	}

	protected String generateOutput(ChannelElement genericChannelElement) {
		@SuppressWarnings("unchecked")
		MRChannelElement<String,Long> channelElement = (MRChannelElement<String,Long>) genericChannelElement;

		return (channelElement.getObject()) +  " - " + channelElement.getValue() + "\n";
	}

	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Please provide an input and an output filename.");

			System.exit(1);
		}

		CountingOutputExtractor extractor = new CountingOutputExtractor(arguments[0], arguments[1]);

		try {
			extractor.run();
		} catch (IOException exception) {
			System.err.println("Error extracting output");

			exception.printStackTrace();
		}
	}
}
