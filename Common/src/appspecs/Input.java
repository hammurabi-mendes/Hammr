package appspecs;

import java.util.List;
import java.util.ArrayList;

import communication.FileChannelHandler;

public class Input {
	private FileChannelHandler channelHandler;

	private List<Node> consumers;

	public Input(FileChannelHandler channelHandler) {
		this.setChannelHandler(channelHandler);

		consumers = new ArrayList<Node>();
	}

	public FileChannelHandler getChannelHandler() {
		return channelHandler;
	}

	public void setChannelHandler(FileChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	public void addConsumer(Node consumer) {
		consumers.add(consumer);
	}

	public List<Node> getConsumers() {
		return consumers;
	}
}
