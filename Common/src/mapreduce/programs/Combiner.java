/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package mapreduce.programs;


import java.io.IOException;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import mapreduce.communication.MRChannelElement;
import communication.channel.ChannelElement;
import communication.writer.ChannelElementWriter;

public abstract class Combiner<O,V> implements ChannelElementWriter {
	private static final long serialVersionUID = 1L;
	private ChannelElementWriter writer = null;
	
	private Map<O,V> currentValues = new HashMap<O,V>();

	@Override
	public final boolean write(ChannelElement elt) throws IOException
	{
		MRChannelElement<O,V> mrelt = (MRChannelElement<O,V>) elt;
		add(mrelt.getKey(), mrelt.getValue());
		return true;
	}
	
	public void initialize(ChannelElementWriter writer)
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
