package mapreduce.programs;

import java.util.Collections;
import java.util.Comparator;

import java.util.Set;

import java.util.List;
import java.util.ArrayList;

import mapreduce.communication.MRChannelElement;

import appspecs.Node;

public abstract class Merger<O,V> extends Node {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void run() {
		Set<String> inputs = getInputChannelNames();

		while(true) {
			List<MRChannelElement<O,V>> channelElements = new ArrayList<MRChannelElement<O,V>>();

			MRChannelElement<O,V> channelElement;

			for(String input: inputs) {
				channelElement = (MRChannelElement<O,V>) read(input);

				if(channelElement != null) {
					channelElements.add(channelElement);
				}
			}

			if(channelElements.size() == 0) {
				break;
			}

			Collections.sort(channelElements, getComparator());

			for(MRChannelElement<O,V> currentChannelElement: channelElements) {
				writeSomeone(currentChannelElement);
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
