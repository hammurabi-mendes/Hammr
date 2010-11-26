package communication;

import java.io.IOException;

import java.io.OutputStream;
import java.io.ObjectOutputStream;

public class ChannelElementOutputStream extends ObjectOutputStream {
	public ChannelElementOutputStream(OutputStream outputStream) throws IOException {
		super(outputStream);
	}

	public void writeChannelElement(ChannelElement channelElement) throws IOException {
		writeObject(channelElement);
	}
}
