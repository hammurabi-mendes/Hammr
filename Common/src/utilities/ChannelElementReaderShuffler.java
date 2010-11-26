package utilities;

/*
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import communication.ChannelElement;
import communication.ChannelHandler;

import java.io.EOFException;
import java.io.IOException;

public class ChannelElementReaderShuffler {
	private List<ChannelHandler> inputs;

	public ChannelElementReaderShuffler(Map<String, ChannelHandler> inputs) throws IOException {
		this.inputs = new ArrayList<ChannelHandler>();

		this.inputs.addAll(inputs.values());
	}

	public ChannelElement readSomeone() throws EOFException, IOException {
		while(inputs.size() > 0) {
			ChannelHandler channelHandler = inputs.get(0);

			try {
				return channelHandler.read();
			} catch (EOFException exception) {
				inputs.remove(0);
			}
		}

		throw new EOFException();
	}
}
*/

import java.util.Map;

import communication.ChannelElement;
import communication.ChannelHandler;

import communication.SHMChannelElementMultiplexer;

import java.io.EOFException;
import java.io.IOException;

public class ChannelElementReaderShuffler {
	private SHMChannelElementMultiplexer multiplexer;

	public ChannelElementReaderShuffler(Map<String, ChannelHandler> inputs) throws IOException {
		multiplexer = new SHMChannelElementMultiplexer(inputs.keySet());

		Relayer relayer;

		for(String name: inputs.keySet()) {
			relayer = new Relayer(name, inputs.get(name), multiplexer);

			relayer.start();
		}
	}

	public ChannelElement readSomeone() throws EOFException, IOException {
		return multiplexer.read();
	}

	private class Relayer extends Thread {
		private String origin;

		private ChannelHandler channelHandler;

		private SHMChannelElementMultiplexer multiplexer;

		public Relayer(String origin, ChannelHandler channelHandler, SHMChannelElementMultiplexer multiplexer) {
			this.origin = origin;

			this.channelHandler = channelHandler;

			this.multiplexer = multiplexer;
		}

		public void run() {
			ChannelElement channelElement;

			while(true) {
				try {
					channelElement = channelHandler.read();
				} catch (EOFException exception) {
					break;
				} catch (IOException exception) {
					System.err.println("Error reading channel handler for reader shuffler");

					exception.printStackTrace();
					break;
				}

				try {
					multiplexer.write(origin, channelElement);
				} catch (IOException exception) {
					System.err.println("Error writing channel handler data to local reader shuffler multiplexer");

					exception.printStackTrace();
					break;
				}
			}

			multiplexer.close(origin);
		}
	}
}
