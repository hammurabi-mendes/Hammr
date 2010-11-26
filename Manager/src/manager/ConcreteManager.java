package manager;

import interfaces.Launcher;
import interfaces.Manager;

import java.rmi.RemoteException;

import java.util.Random;
import java.util.Collections;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import execinfo.ResultSummary;

import scheduler.Scheduler;
import scheduler.ConcreteScheduler;

import utilities.RMIHelper;

public class ConcreteManager implements Manager {
	private Map<String, Scheduler> runningSchedulers;

	private Map<String, Launcher> registeredLaunchers;

	private Map<String, Map<String, InetSocketAddress>> registeredSocketAddresses;

	private Random random;

	public ConcreteManager() {
		runningSchedulers = Collections.synchronizedMap(new LinkedHashMap<String, Scheduler>());

		registeredLaunchers = Collections.synchronizedMap(new LinkedHashMap<String, Launcher>());

		registeredSocketAddresses = Collections.synchronizedMap(new HashMap<String, Map<String, InetSocketAddress>>());

		random = new Random();
	}

	public boolean registerLauncher(Launcher launcher) {
		String launcherId;

		try {
			launcherId = launcher.getId();

			registeredLaunchers.put(launcherId, launcher);
			System.out.println("Registered launcher with ID " + launcherId);

			return true;
		} catch (RemoteException exception) {
			System.err.println("Unable to get ID for launcher: " + exception.toString());
			exception.printStackTrace();

			return false;
		}
	}

	public boolean registerApplication(ApplicationSpecification applicationSpecification) {
		String application = applicationSpecification.getName();

		if(runningSchedulers.containsKey(application)) {
			System.err.println("Application " + application + " is still running");

			return false;
		}

		Scheduler scheduler = setupApplication(application);

		try {
			if(!scheduler.setup(applicationSpecification)) {
				System.err.println("Error setting up scheduler");

				return false;
			}

			if(!scheduler.scheduleNodeGroupBundle()) {
				System.err.println("Initial schedule indicated that no free node group bundles are present");

				return false;
			}
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Initial schedule indicated an insufficient number of launchers");

			finishApplication(application);
			return false;
		} catch (TemporalDependencyException exception) {
			System.err.println("Scheduler setup found a temporal dependency problem");

			finishApplication(application);
			return false;
		} catch (CyclicDependencyException exception) {
			System.err.println("Scheduler setup found a cyclic dependency problem");

			finishApplication(application);
			return false;
		}

		return true;
	}

	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) throws RemoteException {
		Map<String, InetSocketAddress> applicationRegisteredSocketAddresses = registeredSocketAddresses.get(application);

		if(applicationRegisteredSocketAddresses == null) {
			return false;
		}

		applicationRegisteredSocketAddresses.put(name, socketAddress);

		return true;
	}

	public InetSocketAddress obtainSocketAddress(String application, String name) throws RemoteException {
		Map<String, InetSocketAddress> applicationRegisteredSocketAddresses = registeredSocketAddresses.get(application);

		if(applicationRegisteredSocketAddresses == null) {
			return null;
		}

		while(!applicationRegisteredSocketAddresses.containsKey(name)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException exception) {
				// Don't worry, just try again...
			}
		}

		return applicationRegisteredSocketAddresses.get(name);
	}

	public boolean handleTermination(ResultSummary resultSummary) {
		Scheduler scheduler = runningSchedulers.get(resultSummary.getNodeGroupApplication());

		if(scheduler == null) {
			System.err.println("Unable to locate running scheduler for NodeGroup with application " + resultSummary.getNodeGroupApplication() + " and serial number " + resultSummary.getNodeGroupSerialNumber() + "!");

			return false;
		}

		if(!scheduler.handleTermination(resultSummary.getNodeGroupSerialNumber())) {
			System.err.println("Abnormal termination handling for NodeGroup with application " + resultSummary.getNodeGroupApplication() + " and serial number " + resultSummary.getNodeGroupSerialNumber() + "!");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		}

		try {
			if(scheduler.finished()) {
				finishApplication(resultSummary.getNodeGroupApplication());
			}
			else {
				scheduler.scheduleNodeGroupBundle();
			}

			return true;
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Unable to proceed scheduling for application " + resultSummary.getNodeGroupApplication() + "! Aborting application...");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		}
	}

	private synchronized Scheduler setupApplication(String application) {
		Scheduler scheduler = new ConcreteScheduler(this);

		runningSchedulers.put(application, scheduler);

		registeredSocketAddresses.put(application, new HashMap<String, InetSocketAddress>());

		return scheduler;
	}

	private synchronized boolean finishApplication(String application) {
		Scheduler scheduler = runningSchedulers.get(application);

		if(scheduler == null) {
			return false;
		}

		runningSchedulers.remove(application);

		registeredSocketAddresses.remove(application);

		return true;
	}

	public Launcher getRandomLauncher() {
		ArrayList<Map.Entry<String,Launcher>> aliveLaunchers = new ArrayList<Map.Entry<String,Launcher>>();

		aliveLaunchers.addAll(registeredLaunchers.entrySet());

		while(aliveLaunchers.size() > 0) {
			int randomIndex = Math.abs(random.nextInt() % aliveLaunchers.size());

			Map.Entry<String,Launcher> randomEntry = aliveLaunchers.get(randomIndex);

			String randomLauncherIdentifier = randomEntry.getKey();
			Launcher randomLauncherReference = randomEntry.getValue();

			try {
				assert randomLauncherIdentifier.equals(randomLauncherReference.getId());

				return randomLauncherReference;
			} catch (RemoteException exception) {
				System.err.println("Detected failure for launcher " + randomLauncherIdentifier + ", removing it from list...");

				registeredLaunchers.remove(randomLauncherIdentifier);
				aliveLaunchers.remove(randomEntry);
			}
		}

		return null;
	}

	public static void main(String[] arguments) {
		if(arguments.length != 1) {
			System.err.println("Usage: You must supply the registry location");

			System.exit(1);
		}	

		String registryLocation = arguments[0];

		ConcreteManager concreteManager = new ConcreteManager();

		RMIHelper.exportAndRegisterRemoteObject(registryLocation, "Manager", concreteManager);
	}
}
