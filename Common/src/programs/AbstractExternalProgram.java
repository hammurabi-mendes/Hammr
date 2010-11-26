package programs;

import appspecs.Node;

public abstract class AbstractExternalProgram extends Node implements ExternalProgram {
	static final long serialVersionUID = 1L;

	protected String command;
	protected String parameters;

	public AbstractExternalProgram(String command, String parameters) {
		super();

		this.command = command;
		this.parameters = parameters;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getParameters() {
		return parameters;
	}

	public abstract void run();

	public String toString() {
		return command + " " + parameters;
	}
}
