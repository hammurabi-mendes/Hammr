package mapreduce.programs.counting;

import java.util.Set;
import java.util.Map;

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
		Set<Map.Entry<O,Long>> currentEntries = combiner.getCurrentValues();

		for(Map.Entry<O,Long> currentEntry: currentEntries) {
			O object = currentEntry.getKey();
			Long value = currentEntry.getValue();

			writeSomeone(new MRChannelElement<O,Long>(object, value));
		}
	}
}
