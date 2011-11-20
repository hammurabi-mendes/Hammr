package communication.channel;

import java.io.Serializable;

public class ChannelElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private Object object;
	private String description;

	public ChannelElement(Object object, String description) {
		this.object = object;
		this.description = description;
	}

	public ChannelElement(Object object) {
		this(object, null);
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return object.toString();
	}
}
