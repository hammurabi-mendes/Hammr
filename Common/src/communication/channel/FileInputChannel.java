package communication.channel;

import java.io.IOException;

/**
 * Channel handler representing a distributed input file, a local input file or
 * a distributed input file split.
 * 
 * @author ljin
 * 
 */

public abstract class FileInputChannel extends InputChannel implements FileChannel
{
	private static final long serialVersionUID = 1L;

	// FilePath in the local file system or distributed file system
	protected final String _path;
	// Length of the file/split.
	protected long _length = -1;
	
	public FileInputChannel(String name, String path) {
		super(name);
		_path = path;
	}

	@Override
	public final String getPath() {
		return _path;
	}

	public final void setLength(long length)
	{
		_length = length;
	}
	
	public final long getLength(){
		return _length;
	}
	
	public abstract boolean remove() throws IOException;
	
}
