/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package authentication;

import java.util.Map;
import java.util.HashMap;

import security.authenticators.Authenticator;
import security.authenticators.PasswordAuthenticator;

import utilities.filesystem.FileHelper;
import utilities.filesystem.Protocol;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;

import java.io.FileNotFoundException;

import exceptions.AuthenticationException;

/**
 * Maintains a password authentication database for users and applications, and
 * performs authentication based on them. The passwords are hashed according to
 * the default algorithm in PasswordAuthenticator.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class PasswordAuthenticationDatabase extends AuthenticationDatabase {
	private Map<String, Authenticator> users;
	private Map<String, Authenticator> applications;
	private Map<String, Authenticator> nodes;

	/**
	 * Initialize authenticator, reading databases.
	 */
	public void initialize(String baseDirectory) {
		readUserDB(baseDirectory);
		readApplicationDB(baseDirectory);
		readNodeDB(baseDirectory);
	}

	/**
	 * Reads the user database from disk.
	 */
	@SuppressWarnings("unchecked")
	private void readUserDB(String baseDirectory) {
		try {
			ObjectInputStream usersIS = new ObjectInputStream(FileHelper.openR(FileHelper.getFileInformation(baseDirectory, "users.db", Protocol.POSIX_COMPATIBLE)));

			users = (Map<String, Authenticator>) usersIS.readObject();
		} catch (FileNotFoundException exception) {
			System.err.println("No user database found. Using empty dataset.");

			users = new HashMap<String, Authenticator>();
		} catch (Exception exception) {
			System.err.println("Error opening user database: " + exception);

			System.exit(1);
		}
	}

	/**
	 * Writes the user database into disk.
	 */
	private void writeUserDB(String baseDirectory) {
		ObjectOutputStream userOS;

		try {
			userOS = new ObjectOutputStream(FileHelper.openW(FileHelper.getFileInformation(baseDirectory, "users.db", Protocol.POSIX_COMPATIBLE)));

			userOS.writeObject(users);
		} catch (IOException exception) {
			System.err.println("Error writing user database:" + exception);
		}
	}

	/**
	 * Reads the application database from disk.
	 */
	@SuppressWarnings("unchecked")
	private void readApplicationDB(String baseDirectory) {
		try {
			ObjectInputStream applicationsIS = new ObjectInputStream(FileHelper.openR(FileHelper.getFileInformation(baseDirectory, "applications.db", Protocol.POSIX_COMPATIBLE)));

			applications = (Map<String, Authenticator>) applicationsIS.readObject();
		} catch (FileNotFoundException exception) {
			System.err.println("No application database found. Using empty dataset.");

			applications = new HashMap<String, Authenticator>();
		} catch (Exception exception) {
			System.err.println("Error opening application database: " + exception);

			System.exit(1);
		}
	}

	/**
	 * Writes the application database into disk.
	 */
	private void writeApplicationDB(String baseDirectory) {
		ObjectOutputStream applicationOS;

		try {
			applicationOS = new ObjectOutputStream(FileHelper.openW(FileHelper.getFileInformation(baseDirectory, "applications.db", Protocol.POSIX_COMPATIBLE)));

			applicationOS.writeObject(applications);
		} catch (IOException exception) {
			System.err.println("Error write application database:" + exception);
		}
	}

	/**
	 * Reads the node database from disk.
	 */
	@SuppressWarnings("unchecked")
	private void readNodeDB(String baseDirectory) {
		try {
			ObjectInputStream nodesIS = new ObjectInputStream(FileHelper.openR(FileHelper.getFileInformation(baseDirectory, "nodes.db", Protocol.POSIX_COMPATIBLE)));

			nodes = (Map<String, Authenticator>) nodesIS.readObject();
		} catch (FileNotFoundException exception) {
			System.err.println("No node database found. Using empty dataset.");

			nodes = new HashMap<String, Authenticator>();
		} catch (Exception exception) {
			System.err.println("Error opening node database: " + exception);

			System.exit(1);
		}
	}

	/**
	 * Writes the node database into disk.
	 */
	private void writeNodeDB(String baseDirectory) {
		ObjectOutputStream nodeOS;

		try {
			nodeOS = new ObjectOutputStream(FileHelper.openW(FileHelper.getFileInformation(baseDirectory, "nodes.db", Protocol.POSIX_COMPATIBLE)));

			nodeOS.writeObject(nodes);
		} catch (IOException exception) {
			System.err.println("Error writing node database:" + exception);
		}
	}

	/**
	 * Returns true iff the claimed authenticator matches the authentication information stored in the database for users.
	 * 
	 * @param authenticator Claimed authenticator provided by clients
	 * 
	 * @return True iff the claimed authenticator matches the authentication information stored in the database for users.
	 * 
	 * @throws AuthenticationException If the claimed authentication does not match the database information.
	 */
	public boolean authenticateUser(Authenticator authenticator) throws AuthenticationException {
		PasswordAuthenticator claimedAuthenticator = (PasswordAuthenticator) authenticator;

		PasswordAuthenticator storedAuthenticator;

		if((storedAuthenticator = (PasswordAuthenticator) users.get(claimedAuthenticator.getEntity())) == null) {
			throw new AuthenticationException(authenticator);
		}

		if(!claimedAuthenticator.getPasswordHash().equals(storedAuthenticator.getPasswordHash())) {
			throw new AuthenticationException(authenticator);
		}

		return true;
	}

	/**
	 * Returns true iff the claimed authenticator matches the authentication information stored in the database for applications.
	 * 
	 * @param authenticator Claimed authenticator provided by clients
	 * 
	 * @return True iff the claimed authenticator matches the authentication information stored in the database for applications.
	 * 
	 * @throws AuthenticationException If the claimed authentication does not match the database information.
	 */
	public boolean authenticateApplication(Authenticator authenticator) throws AuthenticationException {
		PasswordAuthenticator claimedAuthenticator = (PasswordAuthenticator) authenticator;

		PasswordAuthenticator storedAuthenticator;

		if((storedAuthenticator = (PasswordAuthenticator) applications.get(claimedAuthenticator.getEntity())) == null) {
			throw new AuthenticationException(authenticator);
		}

		if(!(claimedAuthenticator.getPasswordHash().equals(storedAuthenticator.getPasswordHash()))) {
			throw new AuthenticationException(authenticator);
		}

		return true;
	}

	/**
	 * Returns true iff the claimed authenticator matches the authentication information stored in the database for applications.
	 * 
	 * @param authenticator Claimed authenticator provided by clients
	 * 
	 * @return True iff the claimed authenticator matches the authentication information stored in the database for applications.
	 * 
	 * @throws AuthenticationException If the claimed authentication does not match the database information.
	 */
	public boolean authenticateNode(Authenticator authenticator) throws AuthenticationException {
		PasswordAuthenticator claimedAuthenticator = (PasswordAuthenticator) authenticator;

		PasswordAuthenticator storedAuthenticator;

		if((storedAuthenticator = (PasswordAuthenticator) nodes.get(claimedAuthenticator.getEntity())) == null) {
			throw new AuthenticationException(authenticator);
		}

		if(!(claimedAuthenticator.getPasswordHash().equals(storedAuthenticator.getPasswordHash()))) {
			throw new AuthenticationException(authenticator);
		}

		return true;
	}

	/**
	 * Writes a test database containing some users and applications.
	 */
	public void writeTestDB() {
		users = new HashMap<String, Authenticator>();
		applications = new HashMap<String, Authenticator>();
		nodes = new HashMap<String, Authenticator>();

		users.put("user1", new PasswordAuthenticator("user1", "passuser1"));
		users.put("user2", new PasswordAuthenticator("user2", "passuser2"));

		writeUserDB(".");

		applications.put("app1", new PasswordAuthenticator("app1", "passapp1"));
		applications.put("app2", new PasswordAuthenticator("app2", "passapp2"));

		writeApplicationDB(".");

		nodes.put("node1", new PasswordAuthenticator("node1", "passnode1"));
		nodes.put("node2", new PasswordAuthenticator("node2", "passnode2"));

		writeNodeDB(".");
	}

	/**
	 * Main method: Creates a test authentication database.
	 * 
	 * @param arguments Ignored.
	 */
	public static void main(String arguments[]) {
		PasswordAuthenticationDatabase authenticationDatabase = new PasswordAuthenticationDatabase();

		authenticationDatabase.writeTestDB();
	}
}
