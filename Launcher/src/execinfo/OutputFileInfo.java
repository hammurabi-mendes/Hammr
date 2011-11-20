package execinfo;

import java.io.Serializable;

public final class OutputFileInfo implements Serializable {
	/**
	 * Data structure to store the information of Temp Files used as channels.
	 */
	private static final long serialVersionUID = 1L;
	private String fileName = null;
	// Source node ID
	private String source = null;
	// Target node ID
	private String target = null;
	// Size of file in bytes
	private long size = 0;

	/**
	 * @param fName
	 *            : file name
	 * @param sourceId
	 *            : source node id
	 * @param targetId
	 *            : target node id
	 * @param size
	 *            : file size in bytes
	 * 
	 */
	public OutputFileInfo(String fName, String sourceId, String targetId, long size) {
		fileName = fName;
		source = sourceId;
		target = targetId;
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public String getSource() {
		return source;
	}
	
	public String getTarget() {
		return target;
	}

	public long getSize() {
		return size;
	}
}
