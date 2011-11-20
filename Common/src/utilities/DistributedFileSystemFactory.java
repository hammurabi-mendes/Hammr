package utilities;

import fs.DistributedFileSystem;
import fs.HDFSDriver;

public class DistributedFileSystemFactory {
	private static DistributedFileSystem singleton;

	static 
	{
		String hdfsUri = System.getProperty("hammr.hdfs.uri");
		if(hdfsUri == null)
		{
			Logging.Info("[HDFSDriver] Failed to get hammr.hdfs.uri. Exit.");
			System.exit(1);
		}
		singleton = new HDFSDriver(hdfsUri);
	}
	
	public static DistributedFileSystem getDistributedFileSystem()
	{
		return singleton;
	}
}
