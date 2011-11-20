package utilities;

import java.util.Random;

import java.util.Collection;

import java.util.List;
import java.util.ArrayList;

import communication.channel.ChannelElement;
import communication.channel.OutputChannel;
import communication.writer.ChannelElementWriter;

import java.io.IOException;

public class RandomChannelElementWriterShuffler implements ChannelElementWriter{
	private List<OutputChannel> channelHandlers;

	Random random;

	public RandomChannelElementWriterShuffler(Collection<OutputChannel> channelHandlers) {
		this.channelHandlers = new ArrayList<OutputChannel>(channelHandlers);

		random = new Random();
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		if(channelHandlers.size() == 0) {
			return false;
		}

		int index = random.nextInt(channelHandlers.size());

		OutputChannel channelHandler = channelHandlers.get(index);

		channelHandler.write(channelElement);
		
		return true;
	}

	@Override
	public boolean flush() throws IOException {
		for(OutputChannel channel : channelHandlers)
		{
			channel.flush();
		}
		return true;
	}

	@Override
	public boolean close() throws IOException {
		for(OutputChannel channel : channelHandlers)
		{
			channel.close();
		}
		return true;
	}
}
