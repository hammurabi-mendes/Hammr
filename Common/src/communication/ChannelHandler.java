package communication;

import java.io.EOFException;
import java.io.IOException;

import java.io.Serializable;

public abstract class ChannelHandler implements Serializable {
	private static final long serialVersionUID = 1L;

	private Type type;
	private Mode mode;
	private String name;

	private ChannelElementReader channelElementReader;
	private ChannelElementWriter channelElementWriter;

	public ChannelHandler(Type type, Mode mode, String name) {
		this.type = type;
		this.mode = mode;

		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ChannelElementReader getChannelElementReader() {
		return channelElementReader;
	}

	public void setChannelElementReader(ChannelElementReader channelElementReader) {
		this.channelElementReader = channelElementReader;
	}

	public ChannelElementWriter getChannelElementWriter() {
		return channelElementWriter;
	}

	public void setChannelElementWriter(ChannelElementWriter channelElementWriter) {
		this.channelElementWriter = channelElementWriter;
	}

	public ChannelElement read() throws EOFException, IOException {
		return channelElementReader.read();
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		return channelElementWriter.write(channelElement);
	}

	public boolean close() throws IOException {
		if(channelElementWriter != null) {
			return channelElementWriter.close();
		}

		return true;
	}

	public enum Type {
		SHM, TCP, FILE;
	}

	public enum Mode {
		INPUT, OUTPUT;
	}
}
