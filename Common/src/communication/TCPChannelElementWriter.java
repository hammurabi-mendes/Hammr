package communication;

import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.ObjectOutputStream;

import java.io.IOException;

public class TCPChannelElementWriter implements ChannelElementWriter {
	private String name;
	private ObjectOutputStream objectOutputStream;

	public TCPChannelElementWriter(String name, InetSocketAddress socketAddress) throws IOException {
		this.name = name;

		Socket socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());

		this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

		objectOutputStream.writeObject(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ObjectOutputStream getObjectOutputStream() {
		return objectOutputStream;
	}

	public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
		this.objectOutputStream = objectOutputStream;
	}

	public boolean write(ChannelElement channelElement) throws IOException {
		objectOutputStream.writeObject(channelElement);

		return true;
	}

	public boolean close() throws IOException {
		objectOutputStream.flush();

		objectOutputStream.close();

		return true;
	}
}
