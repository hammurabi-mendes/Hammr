package communication.writer;

import java.io.IOException;

import communication.channel.ChannelElement;

/**
 * Interface implements write(channelElement)
 * @author ljin
 *
 * @param <T>
 */

public interface ChannelElementWriter
{
	public abstract boolean write(ChannelElement channelElement) throws IOException;
	public abstract boolean flush() throws IOException;
	public abstract boolean close() throws IOException;
}
