package communication;

import java.io.IOException;

public interface ChannelElementWriter {
	public abstract boolean write(ChannelElement channelElement) throws IOException;
	public abstract boolean flush() throws IOException;
	public abstract boolean close() throws IOException;
}
