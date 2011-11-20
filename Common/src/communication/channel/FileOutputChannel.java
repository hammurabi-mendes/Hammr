package communication.channel;


public abstract class FileOutputChannel extends OutputChannel implements FileChannel
{

	private static final long serialVersionUID = 1L;
	// FilePath in the local file system or distributed file system
	protected final String _path;
	
	public FileOutputChannel(String name, String path) {
		super(name);
		_path = path;
	}
	
	@Override
	public final String getPath()
	{
		return _path;
	}
}
