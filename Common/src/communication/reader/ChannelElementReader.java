package communication.reader;

import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;

public interface ChannelElementReader
{
	public ChannelElement read() throws EOFException, IOException;
	public void close() throws IOException;
}
