package communication.reader;

import fs.FileSystem;

import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;
import communication.stream.ChannelElementInputStream;

import utilities.Logging;

public class FileChannelElementReader implements ChannelElementReader {
	private ChannelElementInputStream channelElementInputStream;

	public FileChannelElementReader(FileSystem fs, String location) throws FileNotFoundException, IOException {
		channelElementInputStream = new ChannelElementInputStream(fs.open(location));
	}

	@Override
	public synchronized ChannelElement read() throws EOFException, IOException {
		ChannelElement element = channelElementInputStream.readChannelElement();
		//Logging.Info(("[FileChannelElementReader][read] " + element));
		return element;
	}

	@Override
	public void close() throws IOException {
		channelElementInputStream.close();
	}
}
