/*
Copyright (c) 2011, Hammurabi Mendes
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

import java.util.Collections;
import java.util.Collection;

import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.net.InetSocketAddress;

import appspecs.ApplicationSpecification;

import exceptions.InexistentInputException;
import exceptions.InexistentOutputException;

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
	private static ConcreteManager instance;

	private String baseDirectory;

	// Active launchers, mapped by ID
	private Map<String, Launcher> registeredLaunchers;

	// Active applications, mapped by name
	private Map<String, ApplicationInformationHolder> applicationInformationHolders;

	static {
		String registryLocation = System.getProperty("java.rmi.server.location");
		String baseDirectory = System.getProperty("hammr.manager.basedir");

		instance = setupManager(registryLocation, baseDirectory);
	}

	/**
	 * Setups a manager for execution.
	 * 
	 * @param registryLocation Location of the registry used to store the manager reference.
	 * 
	 * @return A manager ready for execution.
	 */
	private static ConcreteManager setupManager(String registryLocation, String baseDirectory) {
		// Initiates a concrete manager

		ConcreteManager manager = new ConcreteManager(baseDirectory);

		// Makes the manager available for remote calls

		RMIHelper.exportAndRegisterRemoteObject(registryLocation, "Manager", manager);

		return manager;
	}

	/**
	 * Return the singleton instance of the manager.
	 * 
	 * @return The singleton instance of the manager.
	 */
	public static ConcreteManager getInstance() {
		return instance;
	}

	/**
	 * Private constructor method, used by the singleton constructor.
	 * 
	 * @param baseDirectory The working directory of the manager.
	 */
	private ConcreteManager(String baseDirectory) {
		this.baseDirectory = baseDirectory;

		this.registeredLaunchers = Collections.synchronizedMap(new LinkedHashMap<String, Launcher>());

		this.applicationInformationHolders = Collections.synchronizedMap(new HashMap<String, ApplicationInformationHolder>());
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

		try {
			ApplicationInformationHolder applicationInformationHolder = setupApplication(applicationName, applicationSpecification);

			Scheduler scheduler = applicationInformationHolder.getApplicationScheduler();

			if(!scheduler.schedule()) {
				System.err.println("Initial schedule indicated that no free node group bundles are present");

				return false;
			}
		} catch (TemporalDependencyException exception) {
			System.err.println("Scheduler setup found a temporal dependency problem");

			finishApplication(applicationName);
			return false;
		} catch (CyclicDependencyException exception) {
			System.err.println("Scheduler setup found a cyclic dependency problem");

			finishApplication(applicationName);
			return false;
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Initial schedule indicated an insufficient number of launchers");

			finishApplication(applicationName);
			return false;
		} catch (InexistentInputException exception) {
			System.err.println("Initial schedule indicated that some files are missing: " + exception.toString());

			finishApplication(applicationName);
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
			if(scheduler.finishedIteration()) {
				scheduler.terminateIteration();

				if(scheduler.finishedApplication()) {
					scheduler.terminateApplication();

					finishApplication(resultSummary.getNodeGroupApplication());
				}
				else {
					scheduler.prepareIteration();
				}
			}
			else {
				scheduler.schedule();
			}

			return true;
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Unable to proceed scheduling for application " + resultSummary.getNodeGroupApplication() + "! Aborting application...");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		} catch (InexistentInputException exception) {
			System.err.println("Necessary input files missing for application" + resultSummary.getNodeGroupApplication() + ":" + exception.toString() + " Aborting application...");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		} catch (InexistentOutputException exception) {
			System.err.println("Necessary output files missing for application " + resultSummary.getNodeGroupApplication() + ":" + exception.toString() + " Aborting application...");

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
	 * 
	 * @throws TemporalDependencyException If the application specification has a temporal dependency problem.
	 * @throws CyclicDependencyException If the application specification has a cyclic dependency problem.
	 * @throws InexistentInputException If one of the inputs for the first iteration are missing.
	 */
	private synchronized ApplicationInformationHolder setupApplication(String applicationName, ApplicationSpecification applicationSpecification) throws TemporalDependencyException, CyclicDependencyException, InexistentInputException {
		ApplicationInformationHolder applicationInformationHolder = new ApplicationInformationHolder();

		Scheduler applicationScheduler = new ConcreteScheduler(this);

		applicationInformationHolder.setApplicationName(applicationName);
		applicationInformationHolder.setApplicationSpecification(applicationSpecification);
		applicationInformationHolder.setApplicationScheduler(applicationScheduler);

		applicationInformationHolders.put(applicationName, applicationInformationHolder);

		applicationInformationHolder.markStart();

		applicationScheduler.prepareApplicaiton(applicationSpecification);
		applicationScheduler.prepareIteration();

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
	 * Returns the list of registered launchers.
	 * 
	 * @return The list of registered launchers.
	 */
	public Collection<Launcher> getRegisteredLaunchers() {
		return registeredLaunchers.values();
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

	// Overrides the basic toString() 
	public String toString() {
		return "Manager running on directory \"" + baseDirectory + "\"";
	}

	/**
	 * Manager startup method.
	 * 
	 * @param arguments A list containing:
	 *        1) The registry location;
	 *        2) The manager working directory.
	 */
	public static void main(String[] arguments) {
		System.out.println("Running " + ConcreteManager.getInstance().toString());
	}
}
