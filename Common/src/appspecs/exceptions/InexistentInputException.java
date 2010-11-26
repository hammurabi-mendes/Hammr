package appspecs.exceptions;

public class InexistentInputException extends Exception {
	private static final long serialVersionUID = 1L;

	private String filename;

	public InexistentInputException(String filename) {
		this.filename = filename;
	}

	public String toString() {
		return "Inexistent input (file " + filename + ")";
	}
}
