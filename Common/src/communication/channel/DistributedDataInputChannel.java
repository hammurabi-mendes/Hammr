package communication.channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utilities.DistributedFileSystemFactory;

/**
 * Abstract class representing a distrbuted input file channel or distributed
 * input file split channel.
 * 
 * @author ljin
 * 
 */

public abstract class DistributedDataInputChannel extends FileInputChannel {
	
	private static final long serialVersionUID = 1L;
	
	// Map of (datanode's hostname, size of data in that datanode)
	protected final Map<String, Long> loc = new HashMap<String, Long>();
	
	public DistributedDataInputChannel(String name, String path) {
		super(name, path);
	}

	/**
	 * Update data locality information
	 * @param hosts
	 * @param blockLength
	 */
	public abstract void updateDataLocation(String[] hosts, long start, long blockLength);
	public final Map<String, Long> getDataLocation()
	{
		return loc;
	}
	
	@Override
	public final boolean remove() throws IOException
	{
		return DistributedFileSystemFactory.getDistributedFileSystem().remove(_path);
	}

}
