/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mapreduce.programs;

import java.util.Comparator;

import java.util.Set;

import java.util.Map;
import java.util.HashMap;

import java.util.PriorityQueue;

import mapreduce.communication.MRChannelElement;

import appspecs.Node;

public abstract class Merger<O,V> extends Node {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void run() {
		Set<String> inputs = getInputChannelNames();

		PriorityQueue<MRChannelElement<O,V>> channelElements = new PriorityQueue<MRChannelElement<O,V>>(inputs.size(), getComparator());

		Map<MRChannelElement<O,V>,String> backwardMapping = new HashMap<MRChannelElement<O,V>,String>();

		MRChannelElement<O,V> channelElement;

		for(String input: inputs) {
			channelElement = (MRChannelElement<O,V>) read(input);

			if(channelElement != null) {
				channelElements.add(channelElement);

				backwardMapping.put(channelElement, input);
			}
		}

		while(channelElements.size() > 0) {
			channelElement = channelElements.poll();

			writeSomeone(channelElement);

			String input = backwardMapping.get(channelElement);

			backwardMapping.remove(channelElement);

			channelElement = (MRChannelElement<O,V>) read(input);

			if(channelElement != null) {
				channelElements.add(channelElement);

				backwardMapping.put(channelElement, input);
			}
		}

		closeOutputs();
	}

	public abstract Comparator<MRChannelElement<O,V>> getComparator();

	public class MRChannelElementComparatorObject<X extends Comparable<X>,Y> implements Comparator<MRChannelElement<X,Y>> {
		public int compare(MRChannelElement<X,Y> first, MRChannelElement<X,Y> second) {
			return first.getObject().compareTo(second.getObject());
		}
	}

	public class MRChannelElementComparatorValue<X,Y extends Comparable<Y>> implements Comparator<MRChannelElement<X,Y>> {
		public int compare(MRChannelElement<X,Y> first, MRChannelElement<X,Y> second) {
			return first.getValue().compareTo(second.getValue());
		}
	}
}
