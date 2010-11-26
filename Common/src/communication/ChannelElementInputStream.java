package communication;

import java.io.IOException;
import java.io.EOFException;

import java.io.InputStream;
import java.io.ObjectInputStream;

public class ChannelElementInputStream extends ObjectInputStream {
	public ChannelElementInputStream(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	public ChannelElement readChannelElement() throws EOFException, IOException {
		try {
			return (ChannelElement) readObject();
		} catch (ClassNotFoundException exception) {
			System.err.println("Error reading from channel: unknown class");

			exception.printStackTrace();

			return null;
		}
	}
}
