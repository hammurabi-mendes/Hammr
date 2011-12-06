/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mapreduce.programs;

import java.util.Set;
import java.util.Map;

import appspecs.Node;

import mapreduce.communication.MRChannelElement;

public abstract class Mapper<O,V> extends Node {
	private static final long serialVersionUID = 1L;

	protected int numberReducers;

	protected Combiner<O,V> combiner;

	public Mapper(int numberReducers) {
		this(numberReducers, null);
	}

	public Mapper(int numberReducers, Combiner<O,V> combiner) {
		this.numberReducers = numberReducers;

		this.combiner = combiner;
	}

	public int getNumberReducers() {
		return numberReducers;
	}

	public void setNumberReducers(int numberReducers) {
		this.numberReducers = numberReducers;
	}

	public Combiner<O,V> getCombiner() {
		return combiner;
	}

	public void setCombiner(Combiner<O,V> combiner) {
		this.combiner = combiner;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		MRChannelElement<O,V> channelElement;

		while(true) {
			channelElement = (MRChannelElement<O,V>) readSomeone();

			if(channelElement == null) {
				break;
			}

			O object = channelElement.getObject();

			V value = map(object);

			if(combiner == null) {
				channelElement.setValue(value);

				String destination = calculateDestination(object);

				write(channelElement, destination);
			}
			else {
				combiner.add(object, value);
			}
		}

		finalizeMapping();

		shutdown();
	}

	protected String calculateDestination(O object) {
		return "reducer-" + Math.abs(object.hashCode() % numberReducers);
	}

	protected abstract V map(O object);

	protected void finalizeMapping() {
		if(combiner != null) {
			Set<Map.Entry<O,V>> currentEntries = combiner.getCurrentEntries();

			for(Map.Entry<O,V> currentEntry: currentEntries) {
				O object = currentEntry.getKey();
				V value = currentEntry.getValue();

				String destination = calculateDestination(object);

				write(new MRChannelElement<O,V>(object, value), destination);
			}
		}
	}
}
