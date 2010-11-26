package appspecs.exceptions;

public class OverlappingOutputException extends Exception {
	private static final long serialVersionUID = 1L;

	private String filename;

	public OverlappingOutputException(String filename) {
		this.filename = filename;
	}

	public String toString() {
		return "Overlapping output (file " + filename + ")";
	}
}
