package mapreduce.programs.counting;

import mapreduce.programs.Mapper;

public class CountingMapper<O> extends Mapper<O,Long> {
	private static final long serialVersionUID = 1L;

	public CountingMapper(int numberReducers) {
		super(numberReducers, new CountingCombiner<O>());
	}

	public Long map(O object) {
		return 1L;
	}
}
