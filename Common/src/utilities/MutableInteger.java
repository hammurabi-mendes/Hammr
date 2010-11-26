package utilities;

import java.io.Serializable;

public class MutableInteger implements Serializable {
	private static final long serialVersionUID = 1L;

	private int value;

	public MutableInteger(int value) {
		setValue(value);
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
