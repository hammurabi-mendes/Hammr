package communication.writer;

import java.io.IOException;

import communication.channel.ChannelElement;
import communication.stream.AbstractChannelElementOutputStream;

public final class FileChannelElementWriter implements ChannelElementWriter{
	private final AbstractChannelElementOutputStream channelElementOutputStream;
	
	public FileChannelElementWriter(AbstractChannelElementOutputStream oStream) throws IOException 
	{
		channelElementOutputStream = oStream;
	}

	@Override
	public synchronized boolean write(ChannelElement channelElement) throws IOException {
		channelElementOutputStream.writeChannelElement(channelElement);
		return true;
	}

	@Override
	public synchronized boolean flush() throws IOException {
		channelElementOutputStream.flush();
		return true;
	}

	@Override
	public synchronized boolean close() throws IOException {
		channelElementOutputStream.flush();
		channelElementOutputStream.close();
		return true;
	}
}
