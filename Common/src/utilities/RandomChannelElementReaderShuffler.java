package utilities;

import java.util.Map;

import communication.channel.ChannelElement;

import communication.channel.InputChannel;
import communication.reader.ChannelElementReader;
import communication.reader.SHMChannelElementMultiplexer;

import java.io.EOFException;
import java.io.IOException;

public class RandomChannelElementReaderShuffler implements ChannelElementReader {
	private SHMChannelElementMultiplexer multiplexer;

	public RandomChannelElementReaderShuffler(Map<String, InputChannel> inputs) throws IOException {
		//System.out.println("[RandomChannelElementReaderShuffler] Creating instance... Input: " + inputs);
		multiplexer = new SHMChannelElementMultiplexer(inputs.keySet());

		Relayer relayer;

		for (String name : inputs.keySet()) {
			relayer = new Relayer(name, inputs.get(name), multiplexer);

			relayer.start();
		}
		//System.out.println("[RandomChannelElementReaderShuffler] Creating instance Done. Input: " + inputs);
	}

	@Override
	public ChannelElement read() throws EOFException, IOException {
		return multiplexer.read();
	}

	private class Relayer extends Thread {
		private String origin;

		private InputChannel channelHandler;

		private SHMChannelElementMultiplexer multiplexer;

		private int count = 0;
		
		public Relayer(String origin, InputChannel channelHandler, SHMChannelElementMultiplexer multiplexer) {
			this.origin = origin;

			this.channelHandler = channelHandler;

			this.multiplexer = multiplexer;
			
		}

		public void run() {
			ChannelElement channelElement;
			
			while(true) {
				try {
					//System.out.println("[Relayer][read]");
					count++;
					channelElement = channelHandler.read();
				} catch (EOFException exception) {
					break;
				} catch (IOException exception) {
					Logging.Info("Error reading channel handler for reader shuffler");

					exception.printStackTrace();
					break;
				}

				try {
					multiplexer.write(origin, channelElement);
				} catch (IOException exception) {
					Logging.Info("Error writing channel handler data to local reader shuffler multiplexer");

					exception.printStackTrace();
					break;
				}
			}
			//System.out.println("[Relayer][read] count: " + count);
			multiplexer.close(origin);
		}
	}

	@Override
	public void close() throws IOException {
		multiplexer.close();
	}
}
