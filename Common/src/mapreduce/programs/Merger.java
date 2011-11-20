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

			write(channelElement);

			String input = backwardMapping.get(channelElement);

			backwardMapping.remove(channelElement);

			channelElement = (MRChannelElement<O,V>) read(input);

			if(channelElement != null) {
				channelElements.add(channelElement);

				backwardMapping.put(channelElement, input);
			}
		}

		flushAndCloseOutputs();
	}

	public abstract Comparator<MRChannelElement<O,V>> getComparator();

	public class MRChannelElementComparatorObject<X extends Comparable<X>,Y> implements Comparator<MRChannelElement<X,Y>> {
		public int compare(MRChannelElement<X,Y> first, MRChannelElement<X,Y> second) {
			return first.getKey().compareTo(second.getKey());
		}
	}

	public class MRChannelElementComparatorValue<X,Y extends Comparable<Y>> implements Comparator<MRChannelElement<X,Y>> {
		public int compare(MRChannelElement<X,Y> first, MRChannelElement<X,Y> second) {
			return first.getValue().compareTo(second.getValue());
		}
	}
}
