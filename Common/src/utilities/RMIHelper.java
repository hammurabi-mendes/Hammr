package utilities;

import java.rmi.RMISecurityManager;
import java.rmi.Remote;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

public class RMIHelper {
	public static void exportRemoteObject(Remote object) {
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			UnicastRemoteObject.exportObject(object, 0);
		} catch (Exception exception) {
			System.err.println("Error exporting or registering object: " + exception.toString());
			exception.printStackTrace();
		}
	}

	public static void exportAndRegisterRemoteObject(String name, Remote object) {
		exportAndRegisterRemoteObject(null, name, object);
	}

	public static void exportAndRegisterRemoteObject(String registerLocation, String name, Remote object) {
		if(System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			Remote stub = UnicastRemoteObject.exportObject(object, 0);

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
