package mapreduce.programs;


import java.io.IOException;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import mapreduce.communication.MRChannelElement;
import communication.writer.ChannelElementWriter;

public abstract class Combiner<O,V> implements ChannelElementWriter<MRChannelElement<O,V>> {
	private static final long serialVersionUID = 1L;
	private ChannelElementWriter<MRChannelElement<O,V>> writer = null;
	
	private Map<O,V> currentValues = new HashMap<O,V>();

	@Override
	public final boolean write(MRChannelElement<O,V> elt) throws IOException
	{
		add(elt.getKey(), elt.getValue());
		return true;
	}
	
	public void initialize(ChannelElementWriter<MRChannelElement<O,V>> writer)
	{
		this.writer = writer;
	}
	
	private void add(O object, V newValue) {
		V updatedValue;

		V oldValue = currentValues.get(object);

		if(oldValue != null) {
			updatedValue = combine(oldValue, newValue);
		}
		else {
			updatedValue = newValue;
		}

		currentValues.put(object, updatedValue);
	}
	
	public V get(O object) {
		return currentValues.get(object);
	}

	public Set<O> getCurrentObjects() {
		return currentValues.keySet();
	}
	
	public Set<Map.Entry<O,V>> getCurrentEntries() {
		return currentValues.entrySet();
	}

	public abstract V combine(V oldValue, V newValue);
	
	@Override
	public final boolean flush() throws IOException
	{
		for(Entry<O,V> entry : currentValues.entrySet())
		{
			writer.write(new MRChannelElement<O,V>(entry.getKey(), entry.getValue()));
		}
		currentValues.clear();
		return writer.flush();
	}
	
	@Override 
	public final boolean close() throws IOException
	{
		return writer.close();
	}
}
