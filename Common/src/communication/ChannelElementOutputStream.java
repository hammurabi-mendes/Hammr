package communication;

import java.io.IOException;

import java.io.OutputStream;
import java.io.ObjectOutputStream;

public class ChannelElementOutputStream extends ObjectOutputStream {
	private static long DEFAULT_WRITE_COUNT_FLUSH = 65535;

	private long writeCounter = 0L;

	public ChannelElementOutputStream(OutputStream outputStream) throws IOException {
		super(outputStream);
	}

	public void writeChannelElement(ChannelElement channelElement) throws IOException {
		writeCounter++;

		if((writeCounter % DEFAULT_WRITE_COUNT_FLUSH) == 0) {
			flush();
			reset();
		}

		writeObject(channelElement);
	}
}
