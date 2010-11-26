package mapreduce.programs;

import appspecs.Node;

import mapreduce.communication.MRChannelElement;

public abstract class Reducer<O,V> extends Node {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void run() {
		MRChannelElement<O,V> channelElement;

		while(true) {
			channelElement = (MRChannelElement<O,V>) readSomeone();

			if(channelElement == null) {
				break;
			}

			reduce(channelElement.getObject(), channelElement.getValue());
		}

		finalizeReduce();

		closeOutputs();
	}

	protected abstract void reduce(O object, V value);
	protected abstract void finalizeReduce();
}
