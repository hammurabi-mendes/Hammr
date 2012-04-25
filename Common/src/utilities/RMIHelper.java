/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities;

import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class RMIHelper {
	public static void createRegistry() {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException exception) {
			System.err.println("Unable to create registry, aborting.");

			System.exit(1);
		}
	}

	public static Remote exportRemoteObject(Remote object) {
		return exportRemoteObject(object, false);
	}

	public static Remote exportRemoteObject(Remote object, boolean ssl) {
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			if(ssl) {
				return UnicastRemoteObject.exportObject(object, 0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
			}
			else {
				return UnicastRemoteObject.exportObject(object, 0);
			}
		} catch (Exception exception) {
			System.err.println("Error exporting or registering object: " + exception.toString());
			exception.printStackTrace();
			
			return null;
		}
	}

	public static void exportAndRegisterRemoteObject(String name, Remote object) {
		exportAndRegisterRemoteObject(null, name, object, false);
	}

	public static void exportAndRegisterRemoteObject(String name, Remote object, boolean ssl) {
		exportAndRegisterRemoteObject(null, name, object, ssl);
	}

	public static void exportAndRegisterRemoteObject(String registerLocation, String name, Remote object, boolean ssl) {
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			Remote stub = null;

			if(ssl) {
				stub = UnicastRemoteObject.exportObject(object, 0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
			}
			else {
				stub = UnicastRemoteObject.exportObject(object, 0);
			}

			Registry registry = LocateRegistry.getRegistry(registerLocation);
			registry.rebind(name, stub);
		} catch (Exception exception) {
			System.err.println("Error exporting or registering object: " + exception.toString());
			exception.printStackTrace();
		}
	}

	public static Remote locateRemoteObject(String name) {
		return locateRemoteObject(null, name);
	}

	public static Remote locateRemoteObject(String registerLocation, String name) {
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			Registry registry = LocateRegistry.getRegistry(registerLocation);

			Remote stub = registry.lookup(name);

			return stub;
		} catch (Exception exception) {
			System.err.println("Error looking for name  \"" + name + "\": " + exception.toString());
			exception.printStackTrace();

			return null;
		}
	}

	public static String getUniqueID() {
		UID id = new UID();

		return id.toString();
	}
}
