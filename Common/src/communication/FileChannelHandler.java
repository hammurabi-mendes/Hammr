package communication;

public class FileChannelHandler extends ChannelHandler {
	private static final long serialVersionUID = 1L;

	private String location;

	public FileChannelHandler(ChannelHandler.Mode mode, String name, String location) {
		super(ChannelHandler.Type.FILE, mode, name);

		setLocation(location);
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}
}
