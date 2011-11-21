package utilities;

public class FileInformation {
	private String location;
	private FileProtocol protocol;

	public FileInformation(String location) {
		this(location, FileProtocol.POSIX_COMPATIBLE);
	}

	public FileInformation(String location, FileProtocol protocol) {
		this.location = location;
		this.protocol = protocol;
	}

	public String getLocation() {
		return location;
	}

	public FileProtocol getProtocol() {
		return protocol;
	}

	public boolean equals(FileInformation other) {
		return (this.getLocation() == other.getLocation() && this.getProtocol() == other.getProtocol());
	}

	public int hashCode() {
		return location.hashCode() + protocol.hashCode();
	}
}
