package fs;

public class BlockLocation {
	private final String[] hosts; // hostnames of datanodes
	private final long offset; // offset of the of the block in the file
	private final long length;
	
	BlockLocation(String[] hosts, long offset, long length){
		this.hosts = hosts;
		this.offset = offset;
		this.length = length;
	}
	
	public String[] getHosts(){
		return hosts;
	}
	
	public long getOffset(){
		return offset;
	}
	
	public long getLength(){
		return length;
	}
}