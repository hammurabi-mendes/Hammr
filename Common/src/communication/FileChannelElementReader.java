package communication;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;

public class FileChannelElementReader implements ChannelElementReader {
	private ChannelElementInputStream channelElementInputStream;

	public FileChannelElementReader(String location) throws FileNotFoundException, IOException {
		channelElementInputStream = new ChannelElementInputStream(new FileInputStream(location));
	}

	public synchronized ChannelElement read() throws EOFException, IOException {
		return channelElementInputStream.readChannelElement();
	}

	public synchronized ChannelElement tryRead() throws EOFException, IOException {
		if(channelElementInputStream.available() > 0) {
			return read();
		}

		return null;
	}
}
