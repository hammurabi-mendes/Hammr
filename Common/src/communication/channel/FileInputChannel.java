package communication.channel;

import java.io.IOException;

import fs.FileSystem;

import utilities.FileHelper;
import utilities.FileInfo;

/**
 * Channel handler representing a distributed input file, a local input file or
 * a distributed input file split.
 * 
 * @author ljin
 * 
 */

public class FileInputChannel extends InputChannel implements FileChannel
{
	private static final long serialVersionUID = 1L;

	// FilePath in the local file system or distributed file system
	protected final String _path;
	// Length of the file/split.

	
	public FileInputChannel(String name, String path) {
		super(name);
		_path = path;
	}

	public final String getPath() {
		return _path;
	}

	public final long getLength(){
		return FileHelper.getLengh(_path);
	}
	
	public  boolean remove() throws IOException
	{
		return FileHelper.remove(_path);
	}

	public FileInfo getFileInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
