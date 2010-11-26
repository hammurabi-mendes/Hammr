package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import execinfo.ResultSummary;

public interface Manager extends Remote {
	public boolean registerLauncher(Launcher launcher) throws RemoteException;

	public boolean registerApplication(ApplicationSpecification applicationSpecification) throws RemoteException;

	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) throws RemoteException;
	public InetSocketAddress obtainSocketAddress(String application, String name) throws RemoteException;

	public boolean handleTermination(ResultSummary resultSummary) throws RemoteException;
}
