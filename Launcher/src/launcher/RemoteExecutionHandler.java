/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package launcher;

import java.io.Serializable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;

import utilities.filesystem.FileHelper;
import utilities.filesystem.Protocol;

import execinfo.NodeGroup;

import interfaces.Manager;

/**
 * This class is responsible for running a specific NodeGroup previously submitted to the Launcher,
 * but executes in a different JVM.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class RemoteExecutionHandler extends ExecutionHandler implements Runnable,Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param manager Reference to the manager.
	 * @param launcher Remote reference to the launcher.
	 * @param nodeGroup NodeGroup that should be run.
	 */
	public RemoteExecutionHandler(Manager manager, NodeGroup nodeGroup) {
		super(manager, nodeGroup);
	}

	/**
	 * Reads the remote execution handler from disk.
	 * 
	 * @param directory Directory to read the handler from.
	 * @param filename Filename used for the handler.
	 */
	public static RemoteExecutionHandler readExecutionHandler(String directory, String filename) {
		try {
			ObjectInputStream inputOS = new ObjectInputStream(FileHelper.openR(FileHelper.getFileInformation(directory, filename, Protocol.POSIX_COMPATIBLE)));

			return (RemoteExecutionHandler) inputOS.readObject();
		} catch (Exception exception) {
			System.err.println("Error opening user database: " + exception);

			return null;
		}
	}

	/**
	 * Writes the remote execution handler into disk.
	 * 
	 * @param directory Directory to write the handler to.
	 * @param filename Filename used for the handler.
	 * @param handler Handler to be written.
	 */
	public static void writeExecutionHandler(String directory, String filename, RemoteExecutionHandler handler) {
		ObjectOutputStream outputOS;

		try {
			outputOS = new ObjectOutputStream(FileHelper.openW(FileHelper.getFileInformation(directory, filename, Protocol.POSIX_COMPATIBLE)));

			outputOS.writeObject(handler);
		} catch (IOException exception) {
			System.err.println("Error writing user database:" + exception);
			exception.printStackTrace();
		}
	}

	public static void main(String[] arguments) {
		if(arguments.length <= 1) {
			System.err.println("Usage: RemoteExecutionHandler <directory> <filename>");

			System.exit(1);
		}

		String directory = arguments[0];
		String filename = arguments[1];

		RemoteExecutionHandler handler = RemoteExecutionHandler.readExecutionHandler(directory, filename);

		if(handler == null) {
			System.err.println("Error opening user database.");

			System.exit(1);
		}

		handler.run();
	}
}
