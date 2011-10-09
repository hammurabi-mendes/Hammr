package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import execinfo.ResultSummary;

/**
 * Manager remote interface. These functions can be called by remote machines.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public interface Manager extends Remote {
	/**
	 * Notifies the manager a new launcher has been started. Called by Launchers.
	 * 
	 * @param launcher Started launcher.
	 * 
	 * @return True unless the launcher is not reachable.
	 */
	public boolean registerLauncher(Launcher launcher) throws RemoteException;

	/**
	 * Submits a new application. Called by clients.
	 * 
	 * @param applicationSpecification Specification of the application that should be run.
	 * 
	 * @return False unless:
	 *         1) No running application has the same name;
	 *         2) The scheduler setup for the application went fine;
	 *         
	 *         In these cases, the method returns true.
	 */
	public boolean registerApplication(ApplicationSpecification applicationSpecification) throws RemoteException;

	/**
	 * Informs a server-side TCP channel socket address to the manager. This is called in the setup of NodeGroups that have
	 * server-side TCP channels. This happens in the Launcher. The corresponding client-side TCP channels query the master for
	 * this information.
	 * 
	 * @param application Name of the application.
	 * @param name Name of the Node with a server-side TCP channel.
	 * @param socketAddress Socket addrss of the server-side TCP channel.
	 * 
	 * @return True unless the map for the specific pair application/node already exists.
	 */
	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) throws RemoteException;
	
	/**
	 * Queries for the socket address for a server-side TCP channel. This is called in the setup of NodeGroups that have
	 * client-side TCP channels. This happens in the Launcher. The corresponding server-side TCP channels inform their socket
	 * address to the manager.
	 * 
	 * @param application Name of the application.
	 * @param name Name of the Node with a server-side TCP channel.
	 * 
	 * @return The socket address associated with the requested TCP channel.
	 */
	public InetSocketAddress obtainSocketAddress(String application, String name) throws RemoteException;

	/**
	 * Notifies the master that a NodeGroup finished execution. This is called by the Launchers.
	 * 
	 * @param resultSummary Summary containing the runtime information regarding the executed NodeGroup.
	 * 
	 * @return True if the information was expected at the time this method is called; false otherwise.
	 */
	public boolean handleTermination(ResultSummary resultSummary) throws RemoteException;
}
