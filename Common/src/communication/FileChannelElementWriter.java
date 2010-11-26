package communication;

import java.io.FileOutputStream;

import java.io.IOException;

public class FileChannelElementWriter implements ChannelElementWriter {
	ChannelElementOutputStream channelElementOutputStream;

	public FileChannelElementWriter(String location) throws IOException {
		channelElementOutputStream = new ChannelElementOutputStream(new FileOutputStream(location));
	}

	public synchronized boolean write(ChannelElement channelElement) throws IOException {
		channelElementOutputStream.writeChannelElement(channelElement);

		return true;
	}

	public synchronized boolean close() throws IOException {
		channelElementOutputStream.flush();

		channelElementOutputStream.close();

		return true;
	}
}
