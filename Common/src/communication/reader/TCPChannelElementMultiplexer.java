package communication.reader;

import java.util.Set;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.io.ObjectInputStream;

import java.io.EOFException;
import java.io.IOException;

import communication.channel.ChannelElement;

public class TCPChannelElementMultiplexer extends SHMChannelElementMultiplexer implements ChannelElementReader {
	private ServerSocket serverSocket;

	public TCPChannelElementMultiplexer(Set<String> origins) throws IOException {
		super(origins);

		serverSocket = new ServerSocket(0);

		TCPAccepter accepter = new TCPAccepter();

		accepter.start();
	}

	public InetSocketAddress getAddress() {
		try {
			return new InetSocketAddress(InetAddress.getLocalHost(), serverSocket.getLocalPort());
		} catch (UnknownHostException exception) {
			System.err.println("Unable to obtain local address");

			exception.printStackTrace();

			return null;
		}
	}

	private class TCPAccepter extends Thread {
		public void run() {
			for(int i = 0; i < origins.size(); i++) {
				try {
					Socket socket = serverSocket.accept();

					TCPRelayer relayer = new TCPRelayer(socket);

					relayer.start();
				} catch (IOException exception) {
					System.err.println("Error accepting client (I/O error)");

					exception.printStackTrace();
				}
			}
		}
	}

	private class TCPRelayer extends Thread {
		private String origin;
		private ObjectInputStream objectInputStream;

		public TCPRelayer(Socket socket) throws IOException {
			this.origin = null;

			this.objectInputStream = new ObjectInputStream(socket.getInputStream());
		}

		public void run() {
			Object object;

			try {
				object = objectInputStream.readObject();

				origin = (String) object;

				while(true) {
					try {
						object = objectInputStream.readObject();
					} catch (EOFException exception) {
						break;
					}

					write(origin, (ChannelElement) object);
				}
			} catch (IOException exception) {
				System.err.println("Error receiving data from client \"" + origin + "\" (I/O error)");

				exception.printStackTrace();
			} catch (ClassNotFoundException exception) {
				System.err.println("Error receiving data from client \"" + origin + "\" (class not found)");

				exception.printStackTrace();
			}
			finally {
				close(origin);
			}
		}
	}
}
