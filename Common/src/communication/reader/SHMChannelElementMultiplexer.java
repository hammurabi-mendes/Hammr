package communication.reader;

import java.util.Collections;

import java.util.Set;
import java.util.HashSet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;

public class SHMChannelElementMultiplexer implements ChannelElementReader
{
	private static int DEFAULT_LIMIT = 32;
	private static int DEFAULT_RETRY = 250;

	protected Set<String> origins;

	protected BlockingQueue<ChannelElement> queue;

	public SHMChannelElementMultiplexer(Set<String> origins) {
		this.origins = Collections.synchronizedSet(new HashSet<String>());

		this.origins.addAll(origins);

		this.queue = new ArrayBlockingQueue<ChannelElement>(DEFAULT_LIMIT);
	}

	@Override
	public ChannelElement read() throws EOFException, IOException {
		ChannelElement channelElement = null;

		while(true) {
			try {
				channelElement = null;
				channelElement = queue.poll(DEFAULT_RETRY, TimeUnit.MILLISECONDS);

				if(channelElement != null) {
					return channelElement;
				}
				else if(origins.size() == 0) {
					//System.out.println("[" + SHMChannelElementMultiplexer.class.getSimpleName() + "][read] EOF.");
					throw new EOFException();
				}
			} catch (InterruptedException exception) {
				System.err.println("Unexpected thread interruption while waiting for a read");

				exception.printStackTrace();
			}
		}
	}

	public ChannelElement tryRead() throws EOFException, IOException {
		return queue.poll();
	}

	public boolean write(String origin, ChannelElement channelElement) throws IOException {
		try {
			queue.put(channelElement);
		} catch (InterruptedException exception) {
			System.err.println("Unexpected thread interruption while waiting for write");

			exception.printStackTrace();
			return false;
		}

		return true;
	}

	public void close(String origin) {
		//System.out.println("[" + SHMChannelElementMultiplexer.class.getSimpleName() + "][close] origin: " + origin);
		boolean result = origins.remove(origin);

		if(result == false) {
			System.err.println("Error deleting origin " + origin + " for SHM channel multiplexer");
		}
	}

	@Override
	public void close() throws IOException {
		System.err.println("Closing without specifying the input is not permitted");

		throw new IOException();
	}
}
