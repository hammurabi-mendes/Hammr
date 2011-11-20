package programs;

import appspecs.Node;

import communication.channel.ChannelElement;

public class ReaderSomeoneWriterSomeone extends Node {
	private static final long serialVersionUID = 1L;

	public void run() {
		ChannelElement channelElement;

		while(true) {
			channelElement = readSomeone();

			if(channelElement == null) {
				break;
			}

			write(channelElement);
		}

		flushAndCloseOutputs();
	}
}
