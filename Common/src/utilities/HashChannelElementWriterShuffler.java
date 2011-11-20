package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mapreduce.communication.MRChannelElement;

import communication.channel.ChannelElement;
import communication.channel.OutputChannel;
import communication.writer.ChannelElementWriter;

public class HashChannelElementWriterShuffler implements ChannelElementWriter {

	private final List<OutputChannel> lWriters;
	
	public HashChannelElementWriterShuffler(Collection<OutputChannel> lWriters)
	{
		this.lWriters = new ArrayList<OutputChannel>(lWriters);
	}
	
	@Override
	public boolean write(ChannelElement elt) throws IOException {
		if(!(elt instanceof MRChannelElement))
		{
			throw new IOException("HashChannelElementWrtierShuffler is only implemented for MRChannelElement!");
		}
		if(lWriters.size() == 0) {
			return false;
		}
		
		MRChannelElement mrelt = (MRChannelElement) elt;
		int index = Math.abs(mrelt.getKey().hashCode()) % (lWriters.size());
		lWriters.get(index).write(mrelt);
		return true;
	}

	@Override
	public boolean flush() throws IOException {
		for(ChannelElementWriter writer : lWriters)
		{
			writer.flush();
		}
		return true;
	}

	@Override
	public boolean close() throws IOException {
		for(ChannelElementWriter writer : lWriters)
		{
			writer.close();
		}
		return true;
	}

}
