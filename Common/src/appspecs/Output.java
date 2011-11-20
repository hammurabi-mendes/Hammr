package appspecs;

import communication.FileChannelHandler;

public class Output {
	private FileChannelHandler channelHandler;

	private Node producer;

	public Output(FileChannelHandler channelHandler) {
		this.setChannelHandler(channelHandler);
	}

	public FileChannelHandler getChannelHandler() {
		return channelHandler;
	}

	public void setChannelHandler(FileChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	public void setProducer(Node producer) {
		this.producer = producer;
	}

	public Node getProducer() {
		return producer;
	}
}
