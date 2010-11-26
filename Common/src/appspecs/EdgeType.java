package appspecs;

public enum EdgeType {
	SHM, TCP, FILE;

	public String toString() {
		String result = "default://";

		switch(this) {
		case SHM:
			result = "shm://";
			break;
		case TCP:
			result = "tcp://";
			break;
		case FILE:
			result = "file://";
			break;
		}

		return result;
	}
}
