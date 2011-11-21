package communication.channel;

import java.io.IOException;
import java.io.OutputStream;

import utilities.FileHelper;
import utilities.FileInfo;


public class FileOutputChannel extends OutputChannel implements FileChannel
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
