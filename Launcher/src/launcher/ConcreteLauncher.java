/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package launcher;

import java.util.Collections;
import java.util.Collection;

import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.rmi.RemoteException;

import execinfo.LauncherStatus;
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
	private static final int NUMBER_SLOTS_DEFAULT = Integer.MAX_VALUE;

	private static ConcreteLauncher instance;

	private Manager manager;

	private String id;

	private LauncherStatus launcherStatus;

	private Map<Long, NodeGroup> nodeGroups;

	private Map<String, Object> launcherCache;

	private ExecutorService executorService;

	static {
		String registryLocation = System.getProperty("java.rmi.server.location");

		instance = setupLauncher(registryLocation);
	}

	/**
	 * Setups a launcher for execution.
	 * 
	 * @param registryLocation Location of the registry used to locate the manager.
	 * 
	 * @return A launcher ready for execution.
	 */
	private static ConcreteLauncher setupLauncher(String registryLocation) {
		try {
			// Initiates a concrete launcher

			ConcreteLauncher launcher = new ConcreteLauncher(registryLocation);

			// Makes the launcher available for remote calls

			RMIHelper.exportRemoteObject(launcher);

			// Registers the launcher with the manager

			launcher.registerLauncher();

			return launcher;
		} catch (RemoteException exception) {
			System.err.println("Unable to contact manager");

			System.exit(1);
		} catch (UnknownHostException exception) {
			System.err.println("Unable to determine local hostname");

			System.exit(1);
		}

		return null;
	}

	/**
	 * Return the singleton instance of the launcher.
	 * 
	 * @return The singleton instance of the launcher.
	 */
	public static ConcreteLauncher getInstance() {
		return instance;
	}

	/**
	 * Private constructor method, used by the singleton constructor.
	 * 
	 * @param registryLocation Location of the Registry where we can find the Manager reference.
	 * 
	 * @throws RemoteException If unable to contact either the registry or the manager.
	 * @throws UnknownHostException If unable to determine the local hostname.
	 */
	private ConcreteLauncher(String registryLocation) throws RemoteException, UnknownHostException {
		manager = (Manager) RMIHelper.locateRemoteObject(registryLocation, "Manager");

		id = "Launcher".concat(RMIHelper.getUniqueID());

		launcherStatus = new LauncherStatus(id, InetAddress.getLocalHost().getHostName(), "default_rack", NUMBER_SLOTS_DEFAULT);

		nodeGroups = Collections.synchronizedMap(new HashMap<Long, NodeGroup>());

		launcherCache = Collections.synchronizedMap(new HashMap<String, Object>());

		executorService = Executors.newCachedThreadPool();
	}

	/**
	 * Register this launcher with the manager.
	 * 
	 * @return True if the registration was successful; false otherwise.
	 */
	private boolean registerLauncher() {
		try {
			manager.registerLauncher(this);

			return true;
		} catch (RemoteException e) {
			System.err.println("Unable to contact manager");

			return false;
		}
	}

	/**
	 * Returns the manager associated with this launcher.
	 * 
	 * @return The manager associated with this launcher.
	 */
	public Manager getManager() {
		return manager;
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
	 * Obtains the status of the Launcher.
	 * 
	 * @return The status of the Launcher.
	 */
	public LauncherStatus getStatus() {
		return launcherStatus;
	}

	/**
	 * Submits a NodeGroup for execution, and adjust the number of occupied slots in the
	 * launcher. Called by the manager.
	 * 
	 * @param nodeGroup NodeGroup to be executed.
	 * 
	 * @return True if the NodeGroup fits into the number of free slots available; false otherwise.
	 */
	public synchronized boolean addNodeGroup(NodeGroup nodeGroup) {
		if(launcherStatus.getFreeSlots() < nodeGroup.getSize()) {
			return false;
		}

		nodeGroups.put(nodeGroup.getSerialNumber(), nodeGroup);

		ExecutionHandler executionHandler = new ExecutionHandler(manager, this, nodeGroup);

		executorService.execute(executionHandler);

		launcherStatus.setOcupiedSlots(launcherStatus.getOcupiedSlots() + nodeGroup.getSize());

		return true;
	}

	/**
	 * Removes a NodeGroup from the list of running NodeGroups, and adjust the number of occupied slots
	 * in the launcher.
	 * 
	 * @param nodeGroup NodeGroup to be removed.
	 * 
	 * @return True if the NodeGroup informed was previously present in the list of running NodeGroups.
	 */
	public synchronized boolean delNodeGroup(NodeGroup nodeGroup) {
		if(nodeGroups.containsKey(nodeGroup.getSerialNumber())) {
			launcherStatus.setOcupiedSlots(launcherStatus.getOcupiedSlots() - nodeGroup.getSize());

			nodeGroups.remove(nodeGroup.getSerialNumber());

			return true;
		}

		return false;
	}

	/**
	 * Obtains the current running NodeGroups. Called by the manager.
	 * 
	 * @return The current running NodeGroups.
	 */
	public Collection<NodeGroup> getNodeGroups() {
		return nodeGroups.values();
	}

	/**
	 * Get the object from the launcher cache associated with the specified entry.
	 * 
	 * @param entry Entry used to index the launcher cache.
	 * 
	 * @return The object from the launcher cache associated with the specified entry.
	 */
	public Object getCacheEntry(String entry) {
		return launcherCache.get(entry);
	}

	/**
	 * Insert or replaces an entry into the launcher cache.
	 * 
	 * @param entry Entry used to index the launcher cache.
	 * @param object Object inserted in the launcher cache.
	 * 
	 * @return The previous object associated with the specified entry.
	 */
	public Object putCacheEntry(String entry, Object object) {
		return launcherCache.put(entry, object);
	}

	/**
	 * Launcher startup method.
	 */
	public static void main(String[] arguments) {
		System.out.println("Running " + ConcreteLauncher.getInstance().getId());
	}	
}
