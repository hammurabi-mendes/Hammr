package launcher;

import java.util.Collections;

import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

import execinfo.NodeGroup;

import utilities.RMIHelper;

import interfaces.Launcher;
import interfaces.Manager;

/**
 * This class is a concrete implementation of a Launcher.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ConcreteLauncher implements Launcher {
	private String id;
	private Manager manager;

	private List<NodeGroup> nodeGroups;

	/**
	 * Constructor method.
	 * 
	 * @param registryLocation Location of the Registry where we can find the Manager reference.
	 * 
	 * @throws RemoteException If unable to contact either the registry or the manager.
	 */
	public ConcreteLauncher(String registryLocation) throws RemoteException {
		id = "Launcher".concat(RMIHelper.getUniqueID());

		manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		nodeGroups = Collections.synchronizedList(new ArrayList<NodeGroup>());
	}

	/**
	 * Register this launcher with the manager.
	 * 
	 * @return True if the registration was successful; false otherwise.
	 */
	public boolean registerLauncher() {
		try {
			manager.registerLauncher(this);

			return true;
		} catch (RemoteException e) {
			System.err.println("Unable to contact manager");

			return false;
		}
	}

	/**
	 * Returns the ID of the launcher.
	 * 
	 * @return The ID of the launcher.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Submits a NodeGroup for execution. Called by the manager.
	 * 
	 * @param nodeGroup NodeGroup to be executed.
	 * 
	 * @return Always true.
	 */
	public boolean addNodeGroup(NodeGroup nodeGroup) {
		nodeGroups.add(nodeGroup);

		ExecutionHandler executionHandler = new ExecutionHandler(manager, this, nodeGroup);

		executionHandler.start();

		return true;
	}

	/**
	 * Removes a NodeGroup from the list of running NodeGroups.
	 * 
	 * @param nodeGroup NodeGroup to be removed.
	 * 
	 * @return True if the NodeGroup informed was previously present in the list of running NodeGroups.
	 */
	public boolean delNodeGroup(NodeGroup nodeGroup) {
		return nodeGroups.remove(nodeGroup);
	}

	/**
	 * Obtains the current running NodeGroups. Called by the manager.
	 * 
	 * @return The current running NodeGroups.
	 */
	public List<NodeGroup> getNodeGroups() {
		return nodeGroups;
	}

	/**
	 * Launcher startup method.
	 * 
	 * @param arguments A list containing:
	 *        1) The registry location.
	 */
	public static void main(String[] arguments) {
		if(arguments.length != 1) {
			System.err.println("Usage: ConcreteLauncher <registry_location>");

			System.exit(1);
		}

		String registryLocation = arguments[0];

		ConcreteLauncher concreteLauncher;

		try {
			// Initiates a concrete launcher and makes it available
			// for remote method calls.
			
			concreteLauncher = new ConcreteLauncher(registryLocation);

			RMIHelper.exportRemoteObject(concreteLauncher);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");

			System.exit(1);
			return;
		}

		concreteLauncher.registerLauncher();
	}
}
