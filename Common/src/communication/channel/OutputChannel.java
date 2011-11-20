package communication.channel;

import java.io.IOException;

import communication.channel.ChannelElement;
import communication.writer.ChannelElementWriter;

public class OutputChannel extends Channel implements ChannelElementWriter
{
	private static final long serialVersionUID = 1L;
	private ChannelElementWriter writer = null;
	
	public OutputChannel(String name) {
		super(name);
	}

	public final void setChannelElementWriter(ChannelElementWriter writer)
	{
		this.writer = writer;
	}
	
	public final boolean write(ChannelElement elt) throws IOException
	{
		//System.out.println("[OutputChannel] write " + elt);
		writer.write(elt);
		return true;
	}
	
	@Override
	public boolean close() throws IOException {
		if(writer != null)
		{
			writer.close();
		}
		return true;
	}

	@Override
	public boolean flush() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
