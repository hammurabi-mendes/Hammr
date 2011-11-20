package communication.stream;

import java.io.IOException;

import java.io.OutputStream;

import communication.channel.ChannelElement;

/**
 * Binary output stream for channel element.
 * 
 * @author ljin
 *
 */

public class ChannelElementOutputStream extends AbstractChannelElementOutputStream {
	private static long DEFAULT_WRITE_COUNT_FLUSH = 65535;

	private long writeCounter = 0L;

	public ChannelElementOutputStream(OutputStream outputStream) throws IOException {
		super(outputStream);
	}

	@Override
	public void writeChannelElement(ChannelElement channelElement) throws IOException {
		writeCounter++;

		if((writeCounter % DEFAULT_WRITE_COUNT_FLUSH) == 0) {
			flush();
			reset();
		}

		writeObject(channelElement);
	}
}
