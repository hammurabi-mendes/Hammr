package communication;

import java.io.EOFException;
import java.io.IOException;

public interface ChannelElementReader {
	public abstract ChannelElement read() throws EOFException, IOException;
	public abstract ChannelElement tryRead() throws EOFException, IOException;
}
