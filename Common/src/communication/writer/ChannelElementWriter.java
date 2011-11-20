package communication.writer;

import java.io.IOException;

import communication.channel.ChannelElement;

/**
 * Interface implements write(channelElement)
 * @author ljin
 *
 * @param <T>
 */

public interface ChannelElementWriter<T extends ChannelElement>
{
	public abstract boolean write(T channelElement) throws IOException;
	public abstract boolean flush() throws IOException;
	public abstract boolean close() throws IOException;
}
