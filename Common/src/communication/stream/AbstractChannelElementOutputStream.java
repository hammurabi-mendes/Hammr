package communication.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import communication.channel.ChannelElement;

/**
 * Abstract outputstream class for writing ChannelElement.
 * 
 * @author ljin
 *
 * @param <T> ChannelElement subclass
 */

public abstract class AbstractChannelElementOutputStream<T extends ChannelElement> extends ObjectOutputStream {

	public AbstractChannelElementOutputStream(OutputStream out) throws IOException {
		super(out);
	}
	
	public abstract void writeChannelElement(T elt) throws IOException;	
}
