package communication.channel;

public class DistributedFileSplitInputChannel extends DistributedDataInputChannel {

	private final long m_lOffset;
	
	public DistributedFileSplitInputChannel(String name, String path, long start, long end) {
		super(name, path);
		m_lOffset = start;
		setLength(end - start);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void updateDataLocation(String[] hosts, long blockStart, long blockLength) {
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] Enter. Name:" + getName());
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] hosts " + Arrays.toString(hosts));
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] blockstart " + blockStart);
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] blocklength " + blockLength);
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] split offset " + m_lOffset);
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] split length " + _length);
		
		long splitStart = m_lOffset;
		long splitEnd = splitStart + getLength();
		long blockEnd = blockStart + blockLength;
		
		long start = Math.max(splitStart, blockStart);
		long end = Math.min(splitEnd, blockEnd);
		
		if(end <= start)
		{
			return;
		}
		else
		{
			for(String host : hosts)
			{
				Long length = null;
				if((length = loc.get(host)) != null){
//					System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] Update Host: " + host + " Length:" + (length + end - start));
					loc.put(host, length + end - start);
				}else{
//					System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] Update Host: " + host + " Length:" + (end - start));
					loc.put(host, end - start);
				}
			}
		}
//		System.out.println("[DistributedFileSplitInputChannel][updateDataLocaiton] Location Map : " + loc);
	}
	
	public long getOffset()
	{
		return m_lOffset;
	}

}
