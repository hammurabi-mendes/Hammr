package mapreduce.programs.counting;

import java.util.Collections;
import java.util.Comparator;

import java.util.Map;
import java.util.Map.Entry;

import java.util.List;
import java.util.ArrayList;

import mapreduce.communication.MRChannelElement;

import mapreduce.programs.Reducer;

public class CountingReducer<O> extends Reducer<O,Long> {
	private static final long serialVersionUID = 1L;

	private CountingCombiner<O> combiner;

	public CountingReducer() {
		this.combiner = new CountingCombiner<O>();
	}

	public void reduce(O object, Long value) {
		combiner.add(object, value);
	}

	public void finalizeReduce() {
		List<Map.Entry<O,Long>> currentEntries = new ArrayList<Map.Entry<O,Long>>(combiner.getCurrentEntries());

		Collections.sort(currentEntries, new CountingEntryComparator<O>());

		for(Map.Entry<O,Long> currentEntry: currentEntries) {
			O object = currentEntry.getKey();
			Long value = currentEntry.getValue();

			writeSomeone(new MRChannelElement<O,Long>(object, value));
		}
	}
}

class CountingEntryComparator<O> implements Comparator<Map.Entry<O,Long>> {
	public int compare(Entry<O, Long> first, Entry<O, Long> second) {
		return first.getValue().compareTo(second.getValue());
	}
}
