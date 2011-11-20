package communication.stream;

import java.io.IOException;
import java.io.EOFException;

import java.io.InputStream;
import java.io.ObjectInputStream;

import communication.channel.ChannelElement;

import utilities.Logging;

public class ChannelElementInputStream extends ObjectInputStream {
	public ChannelElementInputStream(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	public ChannelElement readChannelElement() throws EOFException, IOException {
		try {
			ChannelElement element = (ChannelElement)  readObject();
			//Logging.Info(("[ChannelElementInputStream][readChannelElement] " + element));
			return element;
		} catch (ClassNotFoundException exception) {
			Logging.Info("Error reading from channel: unknown class");

			exception.printStackTrace();

			return null;
		}
	}
}
