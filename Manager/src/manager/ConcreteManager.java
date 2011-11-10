/*
Copyright (c) 2010, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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

/**
 * This class is a concrete implementation of a Manager.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public class ConcreteManager implements Manager {
	private String baseDirectory;

	// Active launchers, mapped by ID
	private Map<String, Launcher> registeredLaunchers;

	// Active applications, mapped by name
	private Map<String, ApplicationInformationHolder> applicationInformationHolders;

	private Random random;

	/**
	 * Constructor method.
	 * 
	 * @param baseDirectory The working directory of the manager.
	 */
	public ConcreteManager(String baseDirectory) {
		this.baseDirectory = baseDirectory;

		this.registeredLaunchers = Collections.synchronizedMap(new LinkedHashMap<String, Launcher>());

		this.applicationInformationHolders = Collections.synchronizedMap(new HashMap<String, ApplicationInformationHolder>());

		this.random = new Random();
	}

	/**
	 * Notifies the manager a new launcher has been started. Called by Launchers.
	 * 
	 * @param launcher Started launcher.
	 * 
	 * @return True unless the launcher is not reachable.
	 */
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
	public boolean registerApplication(ApplicationSpecification applicationSpecification) {
		String applicationName = applicationSpecification.getName();

		// Trying to register an application that's still running

		if(applicationInformationHolders.containsKey(applicationName)) {
			System.err.println("Application " + applicationName + " is still running!");

			return false;
		}

		ApplicationInformationHolder applicationInformationHolder = setupApplication(applicationName, applicationSpecification);

		Scheduler scheduler = applicationInformationHolder.getApplicationScheduler();

		try {
			// Setup the scheduler, and try to schedule an initial wave of NodeGroups

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
	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) throws RemoteException {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + application + "!");

			return false;
		}

		applicationInformationHolder.addRegisteredSocketAddresses(name, socketAddress);

		return true;
	}

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

	/**
	 * Notifies the master that a NodeGroup finished execution. This is called by the Launchers.
	 * 
	 * @param resultSummary Summary containing the runtime information regarding the executed NodeGroup.
	 * 
	 * @return True if the information was expected at the time this method is called; false otherwise.
	 */
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

	/**
	 * Creates a holder containing the application name, specification, and scheduler, and makes it
	 * ready to start executing.
	 * 
	 * @param applicationName Name of the application.
	 * @param applicationSpecification Application specification.
	 * 
	 * @return The newly created holder.
	 */
	private synchronized ApplicationInformationHolder setupApplication(String applicationName, ApplicationSpecification applicationSpecification) {
		ApplicationInformationHolder applicationInformationHolder = new ApplicationInformationHolder();

		applicationInformationHolder.setApplicationName(applicationName);
		applicationInformationHolder.setApplicationSpecification(applicationSpecification);

		applicationInformationHolder.setApplicationScheduler(new ConcreteScheduler(this));

		applicationInformationHolder.markStart();

		applicationInformationHolders.put(applicationName, applicationInformationHolder);

		return applicationInformationHolder;
	}

	/**
	 * Deletes the holder containing the application name, specification, and scheduler, effectively
	 * finishing its execution.
	 * 
	 * @param applicationName Name of the application
	 * 
	 * @return True if the application was finished successfully; false otherwise.
	 */
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

	/**
	 * Obtain the first alive Launcher, selected randomly.
	 * 
	 * @return The first alive Launcher, selected randomly.
	 */
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

	/**
	 * Generates a result containing a summary of the whole application execution.
	 * 
	 * @param application The application being summarized.
	 * @param runningTime Application running time.
	 * @param applicationResultSummaries Result summaries obtained for this application.
	 */
	private void processApplicationResultSummaries(String application, long runningTime, Set<ResultSummary> applicationResultSummaries) {
		ResultGenerator resultGenerator = new ResultGenerator(baseDirectory, application, runningTime, applicationResultSummaries);

		resultGenerator.start();
	}

	/**
	 * Manager startup method.
	 * 
	 * @param arguments A list containing:
	 *        1) The registry location;
	 *        2) The manager working directory.
	 */
	public static void main(String[] arguments) {
		if(arguments.length != 2) {
			System.err.println("Usage: ConcreteManager <registry_location> <base_directory>");

			System.exit(1);
		}	

		String registryLocation = arguments[0];

		// Initiates a concrete manager and makes it available
		// for remote method calls.

		ConcreteManager concreteManager = new ConcreteManager(arguments[1]);

		RMIHelper.exportAndRegisterRemoteObject(registryLocation, "Manager", concreteManager);
	}
}
