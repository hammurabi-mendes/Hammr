package launcher;

import java.util.Collections;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import execinfo.NodeGroup;

import utilities.Logging;
import utilities.RMIHelper;

import interfaces.Launcher;
import interfaces.LauncherStatus;
import interfaces.Manager;

/**
 * This class is a concrete implementation of a Launcher.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ConcreteLauncher implements Launcher {
	private static ConcreteLauncher singleton = null;
	private static Manager manager;
	
	private String _hostName;
	private String id;

	private ExecutorService threadPool = Executors.newCachedThreadPool();
	
	// group of running nodes
	private final Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<String, NodeGroup>();
	private LauncherStatus status = null;

	/**
	 * Launcher initialization routine
	 */
	static {
		Logging.Info("[ConcreteLauncher] Initializing ConcreteLauncher singleton...");
		String rmiRegistryLoc = System.getProperty("java.rmi.server.location");
		Logging.Info("[ConcreteLauncher] RMI Registry: " + rmiRegistryLoc);
		try {
			singleton = new ConcreteLauncher(rmiRegistryLoc);
			RMIHelper.exportRemoteObject(singleton);
		} catch (RemoteException exception) {
			Logging.Info("[ConcreteLauncher] Unable to contact manager");
			System.exit(1);
		}
		singleton.registerLauncher();
		Logging.Info(String.format("[ConcreteLauncher] Launcher %s started.", singleton.getId()));
	}
	
	public static ConcreteLauncher getInstance() {
		return singleton;
	}
	
	public static Manager getManager() {
		return manager;
	}
	/**
	 * Constructor method.
	 * 
	 * @param registryLocation Location of the Registry where we can find the Manager reference.
	 * 
	 * @throws RemoteException If unable to contact either the registry or the manager.
	 */
	public ConcreteLauncher(String registryLocation) throws RemoteException {
		// Use hostname as id;
		try {
			_hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		id = _hostName;

		manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");
		
		status = new LauncherStatus(id, _hostName, "default_rack");
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
		nodeGroups.put(nodeGroup.getId(), nodeGroup);

		Logging.Info("[ConcreteLauncher][addNodeGroup] Creating executor for node group:" + nodeGroup + ". Id: "
				+ nodeGroup.getId());
		ExecutionHandler executionHandler = new ExecutionHandler(nodeGroup);
		threadPool.execute(executionHandler);
		
		// Update Launcher Status
		synchronized (status) {
			int size = nodeGroup.size();
			status.ocupiedSlots += size;
//			if (nodeGroup.getNodeGroupBundle().size() == 1)
//				status.blockableSlots += size;
		}
		
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
		// Update Launcher Status
		synchronized (status) {
			int size = nodeGroup.size();
			status.ocupiedSlots -= nodeGroup.size();
//			if (nodeGroup.getNodeGroupBundle().size() == 1)
//				status.blockableSlots -= size;
		}
		nodeGroups.remove(nodeGroup.getId());
		return true;
	}

	/**
	 * Obtains the current running NodeGroups. Called by the manager.
	 * 
	 * @return The current running NodeGroups.
	 */
	@Override
	public List<NodeGroup> getNodeGroups() throws RemoteException {
		return new ArrayList<NodeGroup>(nodeGroups.values());
	}

	@Override
	public LauncherStatus getStatus() throws RemoteException {
		return status;
	}

	public static void main(String[] arguments) {
	}
}
