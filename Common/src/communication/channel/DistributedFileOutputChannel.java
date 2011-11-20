package communication.channel;

public class DistributedFileOutputChannel extends FileOutputChannel {
	private static final long serialVersionUID = 1L;

	public DistributedFileOutputChannel(String name, String path) {
		super(name, path);
	}

}
