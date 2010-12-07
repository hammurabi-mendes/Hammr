package manager;

import interfaces.Launcher;
import interfaces.Manager;

import java.rmi.RemoteException;

import java.util.Random;
import java.util.Collections;

import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
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
	private String baseDirectory;

	private Map<String, Launcher> registeredLaunchers;

	private Map<String, ApplicationInformationHolder> applicationInformationHolders;

	private Random random;

	public ConcreteManager(String baseDirectory) {
		this.baseDirectory = baseDirectory;

		this.registeredLaunchers = Collections.synchronizedMap(new LinkedHashMap<String, Launcher>());

		this.applicationInformationHolders = Collections.synchronizedMap(new HashMap<String, ApplicationInformationHolder>());

		this.random = new Random();
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
		String applicationName = applicationSpecification.getName();

		if(applicationInformationHolders.containsKey(applicationName)) {
			System.err.println("Application " + applicationName + " is still running!");

			return false;
		}

		ApplicationInformationHolder applicationInformationHolder = setupApplication(applicationName, applicationSpecification);

		Scheduler scheduler = applicationInformationHolder.getApplicationScheduler();

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

			finishApplication(applicationName);
			return false;
		} catch (TemporalDependencyException exception) {
			System.err.println("Scheduler setup found a temporal dependency problem");

			finishApplication(applicationName);
			return false;
		} catch (CyclicDependencyException exception) {
			System.err.println("Scheduler setup found a cyclic dependency problem");

			finishApplication(applicationName);
			return false;
		}

		return true;
	}

	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) throws RemoteException {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + application + "!");

			return false;
		}

		applicationInformationHolder.addRegisteredSocketAddresses(name, socketAddress);

		return true;
	}

	public InetSocketAddress obtainSocketAddress(String application, String name) throws RemoteException {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + application + "!");

			return null;
		}

		while(applicationInformationHolder.getRegisteredSocketAddress(name) == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException exception) {
				// Don't worry, just try again...
			}
		}

		return applicationInformationHolder.getRegisteredSocketAddress(name);
	}

	public boolean handleTermination(ResultSummary resultSummary) {
		String application = resultSummary.getNodeGroupApplication();

		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for NodeGroup with application " + resultSummary.getNodeGroupApplication() + " and serial number " + resultSummary.getNodeGroupSerialNumber() + "!");

			return false;
		}

		Scheduler scheduler = applicationInformationHolder.getApplicationScheduler();

		if(scheduler == null) {
			System.err.println("Unable to locate running scheduler for NodeGroup with application " + resultSummary.getNodeGroupApplication() + " and serial number " + resultSummary.getNodeGroupSerialNumber() + "!");

			return false;
		}

		if(!scheduler.handleTermination(resultSummary.getNodeGroupSerialNumber())) {
			System.err.println("Abnormal termination handling for NodeGroup with application " + resultSummary.getNodeGroupApplication() + " and serial number " + resultSummary.getNodeGroupSerialNumber() + "!");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		}

		applicationInformationHolder.addReceivedResultSummaries(resultSummary);

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

	private synchronized ApplicationInformationHolder setupApplication(String applicationName, ApplicationSpecification applicationSpecification) {
		ApplicationInformationHolder applicationInformationHolder = new ApplicationInformationHolder();

		applicationInformationHolder.setApplicationName(applicationName);
		applicationInformationHolder.setApplicationSpecification(applicationSpecification);

		applicationInformationHolder.setApplicationScheduler(new ConcreteScheduler(this));

		applicationInformationHolder.markStart();

		applicationInformationHolders.put(applicationName, applicationInformationHolder);

		return applicationInformationHolder;
	}

	private synchronized boolean finishApplication(String applicationName) {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(applicationName);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + applicationName + "!");

			return false;
		}

		applicationInformationHolders.remove(applicationName);

		applicationInformationHolder.markFinish();

		processApplicationResultSummaries(applicationName, applicationInformationHolder.getTotalRunningTime(), applicationInformationHolder.getReceivedResultSummaries());

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

	private void processApplicationResultSummaries(String application, long runningTime, Set<ResultSummary> applicationResultSummaries) {
		ResultGenerator resultGenerator = new ResultGenerator(baseDirectory, application, runningTime, applicationResultSummaries);

		resultGenerator.start();
	}

	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Usage: ConcreteManager <registry_location> <base_directory>");

			System.exit(1);
		}	

		String registryLocation = arguments[0];

		ConcreteManager concreteManager = new ConcreteManager(arguments[1]);

		RMIHelper.exportAndRegisterRemoteObject(registryLocation, "Manager", concreteManager);
	}
}
