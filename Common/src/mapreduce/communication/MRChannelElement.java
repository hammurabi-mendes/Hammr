package mapreduce.communication;

import communication.ChannelElement;

public class MRChannelElement<O,V> extends ChannelElement {
	private static final long serialVersionUID = 1L;

	private V value;

	public MRChannelElement(O object, V value) {
		super(object, null);

		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public O getObject() {
		return (O) super.getObject();
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
}
