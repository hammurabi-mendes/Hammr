/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.io.Serializable;

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
	 * Returns the aggregator specified by the application name and variable name.
	 * 
	 * @param application The application name.
	 * @param variable The variable name;
	 * 
	 * @return The aggregator associated to the specified variable in the specified application. 
	 */
	public ApplicationAggregator<? extends Serializable, ? extends Serializable> obtainAggregator(String application, String variable) throws RemoteException;

	/**
	 * Returns the controller specified by the application name and controller name.
	 * 
	 * @param application The application name.
	 * @param name The controller name;
	 * 
	 * @return The controller associated to the specified name in the specified application. 
	 */
	public ApplicationController obtainController(String application, String name) throws RemoteException;

	/**
	 * Notifies the master that a NodeGroup finished execution. This is called by the Launchers.
	 * 
	 * @param resultSummary Summary containing the runtime information regarding the executed NodeGroup.
	 * 
	 * @return True if the information was expected at the time this method is called; false otherwise.
	 */
	public boolean handleTermination(ResultSummary resultSummary) throws RemoteException;
}
