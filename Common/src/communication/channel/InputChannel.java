package communication.channel;

import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;
import communication.reader.ChannelElementReader;

import utilities.Logging;

public abstract class InputChannel extends Channel implements ChannelElementReader
{

	private static final long serialVersionUID = 1L;
	protected ChannelElementReader reader = null;
	
	public InputChannel(String name) {
		super(name);
	}

	public final void setChannelElementReader(ChannelElementReader channelElementReader) {
		this.reader = channelElementReader;
	}
	
	public final ChannelElement read() throws EOFException, IOException {
		return reader.read();
	}
	
	@Override
	public final void close() throws IOException {
		if(reader != null)
		{
			Logging.Info("[InputChannel][close] " + name);
			reader.close();
		}
	}
}
