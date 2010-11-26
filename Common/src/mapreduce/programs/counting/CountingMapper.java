package mapreduce.programs.counting;

import mapreduce.programs.Mapper;

public class CountingMapper<T> extends Mapper<T,Long> {
	private static final long serialVersionUID = 1L;

	public CountingMapper(int numberReducers) {
		super(numberReducers, new CountingCombiner<T>());
	}

	public Long map(T object) {
		return 1L;
	}
}
