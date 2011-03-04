package utilities.counting;

import java.io.IOException;

import utilities.OutputExtractor;

import communication.ChannelElement;
import mapreduce.communication.MRChannelElement;

public class CountingOutputExtractor extends OutputExtractor {
	public CountingOutputExtractor(String[] inputOutputPairs) {
		super(inputOutputPairs);
	}

	public CountingOutputExtractor(String[] inputs, String[] outputs) {
		super(inputs, outputs);
	}

	protected String obtainInformation(ChannelElement genericChannelElement) {
		@SuppressWarnings("unchecked")
		MRChannelElement<String,Long> channelElement = (MRChannelElement<String,Long>) genericChannelElement;

		return (channelElement.getObject()) +  " - " + channelElement.getValue() + "\n";
	}

	public static void main(String[] arguments) {
		CountingOutputExtractor extractor = new CountingOutputExtractor(arguments);

		try {
			extractor.run();
		} catch (IOException exception) {
			System.err.println("Error generating input");

			exception.printStackTrace();
		}
	}
}
