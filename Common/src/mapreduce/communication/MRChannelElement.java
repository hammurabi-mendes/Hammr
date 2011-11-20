package mapreduce.communication;

import communication.channel.ChannelElement;

public class MRChannelElement<K,V> extends ChannelElement {
	private static final long serialVersionUID = 1L;

	private K key;
	private V value;

	public MRChannelElement(K key, V value) {
		super(null, null);

		this.key = key;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return "<" + key + "," + value + ">";
	}
}
