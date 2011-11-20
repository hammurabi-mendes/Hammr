package fs;


import java.io.IOException;


public interface DistributedFileSystem extends FileSystem
{
	public BlockLocation[] getFileBlockLocations(String path) throws IOException ;
	public long getBlockSize(String path) throws IOException;
}
