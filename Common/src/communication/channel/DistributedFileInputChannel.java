package communication.channel;

/**
 * Object representing the file split in the distributed file system. Usually an
 * initial node takes an file split as input channel.
 * 
 * @author ljin
 * 
 */
public class DistributedFileInputChannel extends DistributedDataInputChannel {

	private static final long serialVersionUID = 1L;
	
	public DistributedFileInputChannel(String name, String path) {	
		super(name, path);
	}

	@Override
	public void updateDataLocation(String[] hosts, long start, long blockLength) {
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] Enter. Name: " + getName());
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] " + loc);
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] hosts " + Arrays.toString(hosts));
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] blockstart " + start);
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] blocklength " + blockLength);
//		System.out.println("[DistributedFileInputChannel][updateDataLocaiton] file length " + _length);
		for(String host : hosts){
			Long l = null;
			if((l = loc.get(host)) != null){
				loc.put(host, l + blockLength);
			}else{
				loc.put(host, blockLength);
			}
		}
	}


}
