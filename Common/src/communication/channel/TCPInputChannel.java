package communication.channel;

import java.net.InetSocketAddress;


public class TCPInputChannel extends InputChannel {
	
	private static final long serialVersionUID = 1L;
	private InetSocketAddress socketAddress;

	public TCPInputChannel(String name) {
		super(name);
	}

	public final InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public final void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
}
