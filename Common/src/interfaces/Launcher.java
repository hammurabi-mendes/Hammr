/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package interfaces;

import java.util.Collection;

import java.rmi.Remote;
import java.rmi.RemoteException;

import execinfo.LauncherInformation;

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
	 * Obtains the information regarding the Launcher.
	 * 
	 * @return The information regarding the Launcher.
	 */
	public LauncherInformation getInformation() throws RemoteException;

	/**
	 * Submits a NodeGroup for execution, and adjust the number of occupied slots in the
	 * launcher.
	 * 
	 * @param nodeGroup NodeGroup to be executed.
	 * 
	 * @return True if the NodeGroup fits into the number of free slots available; false otherwise.
	 */
	public boolean addNodeGroup(NodeGroup nodeGroup) throws RemoteException;

	/**
	 * Obtains the current running NodeGroups. Called by the manager.
	 * 
	 * @return The current running NodeGroups.
	 */
	public Collection<NodeGroup> getNodeGroups() throws RemoteException;

	/**
	 * Removes a NodeGroup from the list of running NodeGroups, and adjust the number of occupied slots
	 * in the launcher.
	 * 
	 * @param serialNumber Serial number of the NodeGroup to be removed.
	 * 
	 * @return True if the NodeGroup informed was previously present in the list of running NodeGroups.
	 */
	public boolean delNodeGroup(long serialNumber) throws RemoteException;

	/**
	 * Get the object from the launcher cache associated with the specified entry.
	 * 
	 * @param entry Entry used to index the launcher cache.
	 * 
	 * @return The object from the launcher cache associated with the specified entry.
	 */
	public Object getCacheEntry(String entry) throws RemoteException;

	/**
	 * Insert or replaces an entry into the launcher cache.
	 * 
	 * @param entry Entry used to index the launcher cache.
	 * @param object Object inserted in the launcher cache.
	 * 
	 * @return The previous object associated with the specified entry.
	 */
	public Object putCacheEntry(String entry, Object object) throws RemoteException;
}
