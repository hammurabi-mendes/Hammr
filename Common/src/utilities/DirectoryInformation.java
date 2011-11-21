package utilities;

public class DirectoryInformation {
	private String path;
	private FileProtocol protocol;

	public DirectoryInformation(String location) {
		this(location, FileProtocol.POSIX_COMPATIBLE);
	}

	public DirectoryInformation(String location, FileProtocol protocol) {
		this.path = location;
		this.protocol = protocol;
	}

	public String getPath() {
		return path;
	}

	public FileProtocol getProtocol() {
		return protocol;
	}

	public boolean equals(DirectoryInformation other) {
		return (this.getPath() == other.getPath() && this.getProtocol() == other.getProtocol());
	}

	public int hashCode() {
		return path.hashCode() + protocol.hashCode();
	}
}
