package mapreduce.programs.counting;

import java.io.Serializable;

import mapreduce.programs.Combiner;

public class CountingCombiner<O> extends Combiner<O,Long> implements Serializable {
	private static final long serialVersionUID = 1L;

	public Long combine(Long oldValue, Long newValue) {
		return oldValue + newValue;
	}
}
