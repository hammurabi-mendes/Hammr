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

		closeOutputs();
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
