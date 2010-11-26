package communication;

import java.net.InetSocketAddress;

public class TCPChannelHandler extends ChannelHandler {
	private static final long serialVersionUID = 1L;

	private InetSocketAddress socketAddress;

	public TCPChannelHandler(ChannelHandler.Mode mode, String name) {
		super(ChannelHandler.Type.TCP, mode, name);
	}

	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
}
