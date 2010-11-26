package exceptions;

public class InexistentApplicationException extends Exception {
	private static final long serialVersionUID = 1L;

	private String application;

	public InexistentApplicationException(String application) {
		this.application = application;
	}

	public String toString() {
		return "The application " + application + " does not exist on the manager";
	}
}
