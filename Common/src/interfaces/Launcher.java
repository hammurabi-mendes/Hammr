package interfaces;

import java.util.List;

import java.rmi.Remote;
import java.rmi.RemoteException;

import execinfo.NodeGroup;

public interface Launcher extends Remote {
	public String getId() throws RemoteException;

	public boolean addNodeGroup(NodeGroup nodeGroup) throws RemoteException;
	public List<NodeGroup> getNodeGroups() throws RemoteException;
}
