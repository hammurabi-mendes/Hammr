package communication.stream;

import java.io.IOException;
import java.io.OutputStream;

import communication.channel.ChannelElement;

public class ChannelElementTextOutputStream extends AbstractChannelElementOutputStream{
	private static final String utf8 = "UTF-8";
	
	public ChannelElementTextOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	public void writeChannelElement(ChannelElement elt) throws IOException {
		write(elt.toString().getBytes(utf8));
		write("\n".getBytes(utf8));	
	}

}
