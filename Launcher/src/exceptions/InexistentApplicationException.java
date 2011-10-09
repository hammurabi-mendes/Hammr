package exceptions;

/**
 * Exception that represents that an application is not actively running in the manager
 * and yet some node wants to insert server-side TCP socket addresses or it. 
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class InexistentApplicationException extends Exception {
	private static final long serialVersionUID = 1L;

	private String application;

	/**
	 * Constructor method.
	 * 
	 * @param application Name of the referenced application.
	 */
	public InexistentApplicationException(String application) {
		this.application = application;
	}

	public String toString() {
		return "The application " + application + " does not exist on the manager";
	}
}
