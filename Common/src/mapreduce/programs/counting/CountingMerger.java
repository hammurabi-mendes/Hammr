package mapreduce.programs.counting;

import java.util.Comparator;

import mapreduce.communication.MRChannelElement;
import mapreduce.programs.Merger;

public class CountingMerger<O> extends Merger<O,Long> {
	private static final long serialVersionUID = 1L;

	public Comparator<MRChannelElement<O, Long>> getComparator() {
		return new MRChannelElementComparatorValue<O,Long>();
	}
}

class MRChannelElementComparator<O> implements Comparator<MRChannelElement<O,Long>> {
	public int compare(MRChannelElement<O, Long> first, MRChannelElement<O, Long> second) {
		return first.getValue().compareTo(second.getValue());
	}
}
