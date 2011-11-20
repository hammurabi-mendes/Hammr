package interfaces;

import java.util.List;

import java.rmi.Remote;
import java.rmi.RemoteException;

import execinfo.NodeGroup;

/**
 * Launcher remote interface. These functions can be called by remote machines.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public interface Launcher extends Remote {
	/**
	 * Returns the ID of the launcher.
	 * 
	 * @return The ID of the launcher.
	 */
	public String getId() throws RemoteException;

	/**
	 * Submits a NodeGroup for execution. Called by the manager.
	 * 
	 * @param nodeGroup NodeGroup to be executed.
	 * 
	 * @return Always true.
	 */
	public boolean addNodeGroup(NodeGroup nodeGroup) throws RemoteException;
	
	/**
	 * Obtains the current running NodeGroups. Called by the manager.
	 * 
	 * @return The current running NodeGroups.
	 */
	public List<NodeGroup> getNodeGroups() throws RemoteException;
	
	/**
	 * Obtains the current status. Called by the manager
	 * @return
	 * @throws RemoteException
	 */
	
	public LauncherStatus getStatus() throws RemoteException;
}
