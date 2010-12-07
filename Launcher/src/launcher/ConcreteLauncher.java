package launcher;

import java.util.Collections;

import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

import execinfo.NodeGroup;

import utilities.RMIHelper;

import interfaces.Launcher;
import interfaces.Manager;

public class ConcreteLauncher implements Launcher {
	private String id;
	private Manager manager;

	private List<NodeGroup> nodeGroups;

	public ConcreteLauncher(String registryLocation) throws RemoteException {
		id = "Launcher".concat(RMIHelper.getUniqueID());

		manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		nodeGroups = Collections.synchronizedList(new ArrayList<NodeGroup>());
	}

	public boolean registerLauncher() {
		try {
			manager.registerLauncher(this);

			return true;
		} catch (RemoteException e) {
			System.err.println("Unable to contact manager");

			return false;
		}
	}

	public String getId() {
		return id;
	}

	public boolean addNodeGroup(NodeGroup nodeGroup) {
		nodeGroups.add(nodeGroup);

		ExecutionHandler executionHandler = new ExecutionHandler(manager, this, nodeGroup);

		executionHandler.start();

		return true;
	}

	public boolean delNodeGroup(NodeGroup nodeGroup) {
		return nodeGroups.remove(nodeGroup);
	}

	public List<NodeGroup> getNodeGroups() {
		return nodeGroups;
	}

	public static void main(String[] arguments) {
		if(arguments.length != 1) {
			System.err.println("Usage: ConcreteLauncher <registry_location>");

			System.exit(1);
		}

		String registryLocation = arguments[0];

		ConcreteLauncher concreteLauncher;

		try {
			concreteLauncher = new ConcreteLauncher(registryLocation);

			RMIHelper.exportAndRegisterRemoteObject(registryLocation, concreteLauncher.getId(), concreteLauncher);
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");

			System.exit(1);
			return;
		}

		concreteLauncher.registerLauncher();
	}
}
