package utilities;

import java.util.Random;

import java.util.Collection;

import java.util.List;
import java.util.ArrayList;

import communication.ChannelElement;
import communication.ChannelHandler;

import java.io.IOException;

public class ChannelElementWriterShuffler {
	private List<ChannelHandler> channelHandlers;

	Random random;

	public ChannelElementWriterShuffler(Collection<ChannelHandler> channelHandlers) {
		this.channelHandlers = new ArrayList<ChannelHandler>(channelHandlers);

		random = new Random();
	}

	public boolean writeSomeone(ChannelElement channelElement) throws IOException {
		if(channelHandlers.size() == 0) {
			return false;
		}

		int index = random.nextInt(channelHandlers.size());

		ChannelHandler channelHandler = channelHandlers.get(index);

		return channelHandler.write(channelElement);
	}
}
