package communication;

import java.io.IOException;

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

	public boolean close() throws IOException {
		channelElementMultiplexer.close(name);

		return true;
	}
}
