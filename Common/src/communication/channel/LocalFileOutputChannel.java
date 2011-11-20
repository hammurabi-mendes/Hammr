package communication.channel;


public class LocalFileOutputChannel extends FileOutputChannel {

	private static final long serialVersionUID = 1L;

	public LocalFileOutputChannel(String name, String path) {
		super(name, path);
	}

}
