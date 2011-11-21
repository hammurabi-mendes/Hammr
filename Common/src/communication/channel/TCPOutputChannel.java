package communication.channel;

import java.net.InetSocketAddress;

public class TCPOutputChannel extends OutputChannel {

	private static final long serialVersionUID = 1L;
	private InetSocketAddress socketAddress;
	
	public TCPOutputChannel(String name) {
		super(name);
	}

	public final InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public final void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
}
