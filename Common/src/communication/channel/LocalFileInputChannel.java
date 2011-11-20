package communication.channel;

import java.io.IOException;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Object representing a remote local input file.
 * @author ljin
 *
 */

public class LocalFileInputChannel extends FileInputChannel
{
	private static final long serialVersionUID = 1L;
	// Canonical hostname of the server holding this file
	private String host = null;	
	
	public LocalFileInputChannel(String name, String location) {
		super(name, location);
	}

	public String getHost() {
		return host;
	}
	
	public void setHost(String canonicalHostName) {
		host = canonicalHostName;
	}

	@Override
	public boolean remove() throws IOException {
		throw new NotImplementedException();
	}
}
