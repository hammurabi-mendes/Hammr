package communication;

public class SHMChannelHandler extends ChannelHandler {
	private static final long serialVersionUID = 1L;

	public SHMChannelHandler(ChannelHandler.Mode mode, String name) {
		super(ChannelHandler.Type.SHM, mode, name);
	}
}
