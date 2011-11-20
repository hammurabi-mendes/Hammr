package communication.writer;

import java.io.IOException;

import communication.channel.ChannelElement;
import communication.reader.SHMChannelElementMultiplexer;

public class SHMChannelElementWriter implements ChannelElementWriter {
	private String name;
	private SHMChannelElementMultiplexer channelElementMultiplexer;

	public SHMChannelElementWriter(String name, SHMChannelElementMultiplexer shmChannelElementMultiplexer) {
		this.name = name;

		this.channelElementMultiplexer = shmChannelElementMultiplexer;
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		channelElementMultiplexer.write(name, channelElement);

		return true;
	}

	public boolean flush() throws IOException {
		return true;
	}

	public boolean close() throws IOException {
		channelElementMultiplexer.close(name);

		return true;
	}
}
