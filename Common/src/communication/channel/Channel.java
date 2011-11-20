package communication.channel;

import java.io.Serializable;

/**
 * Object representing a communication channel. 
 * 
 * @author ljin
 * 
 */

public abstract class Channel implements Serializable {
	private static final long serialVersionUID = 1L;
	protected final String name;

	public Channel(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return name;
	}	
}
