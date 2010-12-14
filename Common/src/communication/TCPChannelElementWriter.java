package communication;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.IOException;

public class TCPChannelElementWriter implements ChannelElementWriter {
	private String name;
	private ChannelElementOutputStream channelElementOutputStream;

	public TCPChannelElementWriter(String name, InetSocketAddress socketAddress) throws IOException {
		this.name = name;

		Socket socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());

		this.channelElementOutputStream = new ChannelElementOutputStream(socket.getOutputStream());

		channelElementOutputStream.writeObject(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		channelElementOutputStream.writeChannelElement(channelElement);

		return true;
	}

	public boolean flush() throws IOException {
		channelElementOutputStream.flush();
		channelElementOutputStream.reset();

		return true;
	}

	public boolean close() throws IOException {
		channelElementOutputStream.flush();

		channelElementOutputStream.close();

		return true;
	}
}
