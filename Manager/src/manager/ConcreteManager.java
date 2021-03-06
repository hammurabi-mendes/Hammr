/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package manager;

import interfaces.ApplicationAggregator;
import interfaces.ApplicationController;
import interfaces.Launcher;
import interfaces.Manager;

import java.rmi.RemoteException;

import java.util.Collections;
import java.util.Collection;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.Serializable;

import java.net.InetSocketAddress;

import appspecs.Node;
import appspecs.ApplicationSpecification;

import authentication.AuthenticationDatabase;
import authentication.PasswordAuthenticationDatabase;

import exceptions.InexistentInputException;
import exceptions.InexistentOutputException;

import exceptions.AuthenticationException;

import exceptions.ParsingNodeGroupPlacementException;

import exceptions.RuntimeGlobalPlacementException;
import exceptions.RuntimeNodeGroupPlacementException;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import execinfo.LauncherInformation;
import execinfo.ResultSummary;

import scheduler.Scheduler;
import scheduler.ConcreteScheduler;

import security.CollocationStatus;
import security.authenticators.Authenticator;
import security.restrictions.LauncherRestrictions;

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
	private Map<String, Launcher> launcherMap;

	private Map<String, LauncherInformation> launcherInformationMap;

	// Active applications, mapped by name
	private Map<String, ApplicationInformationHolder> applicationInformationHolders;

	// Information about user and application authentication
	private AuthenticationDatabase authenticationDatabase;

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

		// Makes the manager available for remote calls, using SSL

		RMIHelper.exportAndRegisterRemoteObject(registryLocation, "Manager", manager, true);

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

		this.launcherMap = Collections.synchronizedMap(new LinkedHashMap<String, Launcher>());

		this.launcherInformationMap = Collections.synchronizedMap(new LinkedHashMap<String, LauncherInformation>());

		this.applicationInformationHolders = Collections.synchronizedMap(new HashMap<String, ApplicationInformationHolder>());

		this.authenticationDatabase = new PasswordAuthenticationDatabase();

		((PasswordAuthenticationDatabase) authenticationDatabase).initialize(baseDirectory);
	}

	/**
	 * Returns the base working directory of the Manager.
	 * 
	 * @return Base directory of the Manager.
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * Notifies the manager a new launcher has been started. Called by Launchers.
	 * 
	 * @param launcher Started launcher.
	 * 
	 * @return True unless the launcher is not reachable.
	 */
	public boolean registerLauncher(Launcher launcher) {
		try {
			LauncherInformation launcherInformation = launcher.getInformation();

			launcherMap.put(launcherInformation.getId(), launcher);

			launcherInformationMap.put(launcherInformation.getId(), launcherInformation);

			System.out.println("Registered launcher with ID " + launcherInformation.getId());

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
			// Authenticate User, Application, and Nodes, if the authenticators are present

			if(applicationSpecification.getUserAuthenticator() != null) {
				authenticationDatabase.authenticateUser(applicationSpecification.getUserAuthenticator());
			}

			if(applicationSpecification.getApplicationAuthenticator() != null) {
				authenticationDatabase.authenticateApplication(applicationSpecification.getApplicationAuthenticator());
			}

			Map<Node,Authenticator> nodeAuthenticators = applicationSpecification.getNodeAuthenticators();

			for(Node node: nodeAuthenticators.keySet()) {
				Authenticator authenticator = nodeAuthenticators.get(node);

				authenticationDatabase.authenticateNode(authenticator);
			}

			ApplicationInformationHolder applicationInformationHolder = setupApplication(applicationName, applicationSpecification);

			Scheduler scheduler = applicationInformationHolder.getApplicationScheduler();

			if(!scheduler.schedule()) {
				System.err.println("Initial schedule indicated that no free node group bundles are present");

				finishApplication(applicationName);
				return false;
			}
		} catch (AuthenticationException exception) {
			System.err.println("Initial authentication failed: "+ exception.toString());

			return false;
		} catch (TemporalDependencyException exception) {
			System.err.println("Scheduler setup found a temporal dependency problem");

			finishApplication(applicationName);
			return false;
		} catch (CyclicDependencyException exception) {
			System.err.println("Scheduler setup found a cyclic dependency problem");

			finishApplication(applicationName);
			return false;
		} catch (ParsingNodeGroupPlacementException exception) {
			System.err.println("Initial schedule indicated authentication faiure or inability to meet security criteria");

			finishApplication(applicationName);
			return false;
		} catch (InexistentInputException exception) {
			System.err.println("Initial schedule indicated that some files are missing: " + exception.toString());

			finishApplication(applicationName);
			return false;
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Insuficient number of Launchers to run the application");

			finishApplication(applicationName);
			return false;
		} catch (RuntimeGlobalPlacementException exception) {
			System.err.println("Insuficient number of Launchers that meet global security criteria to run the application");

			finishApplication(applicationName);
			return false;
		} catch (RuntimeNodeGroupPlacementException exception) {
			System.err.println("Insuficient number of Launchers that meet fine-grained node security criteria to run the application");

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
	public boolean insertSocketAddress(String application, String name, InetSocketAddress socketAddress) {
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
	public InetSocketAddress obtainSocketAddress(String application, String name) {
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
	 * Returns the aggregator specified by the application name and variable name.
	 * 
	 * @param application The application name.
	 * @param variable The variable name;
	 * 
	 * @return The aggregator associated to the specified variable in the specified application. 
	 */
	public ApplicationAggregator<? extends Serializable, ? extends Serializable> obtainAggregator(String application, String variable) {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + application + "!");

			return null;
		}

		return applicationInformationHolder.getApplicationSpecification().getAggregator(variable);
	}


	/**
	 * Returns the controller specified by the application name and controller name.
	 * 
	 * @param application The application name.
	 * @param name The controller name;
	 * 
	 * @return The controller associated to the specified name in the specified application. 
	 */
	public ApplicationController obtainController(String application, String name) {
		ApplicationInformationHolder applicationInformationHolder = applicationInformationHolders.get(application);

		if(applicationInformationHolder == null) {
			System.err.println("Unable to locate application information holder for application " + application + "!");

			return null;
		}

		return applicationInformationHolder.getApplicationSpecification().getController(name);
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

		// Inserts the result summary and updates the aggregators

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

					scheduler.schedule();
				}
			}
			else {
				scheduler.schedule();
			}

			return true;
		} catch (RuntimeGlobalPlacementException exception) {
			System.err.println("Unable to proceed scheduling for application " + resultSummary.getNodeGroupApplication() + ":" + exception.toString() + " Aborting application...");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		} catch (RuntimeNodeGroupPlacementException exception) {
			System.err.println("Unable to proceed scheduling for application " + resultSummary.getNodeGroupApplication() + ":" + exception.toString() + " Aborting application...");

			finishApplication(resultSummary.getNodeGroupApplication());
			return false;
		} catch (InsufficientLaunchersException exception) {
			System.err.println("Unable to proceed scheduling for application " + resultSummary.getNodeGroupApplication() + ":" + exception.toString() + " Aborting application...");

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
	 * @throws ParsingNodeGroupPlacementException  If the node placement restrictions are incompatible with the graph parsing.
	 */
	private synchronized ApplicationInformationHolder setupApplication(String applicationName, ApplicationSpecification applicationSpecification) throws TemporalDependencyException, CyclicDependencyException, InexistentInputException, ParsingNodeGroupPlacementException {
		ApplicationInformationHolder applicationInformationHolder = new ApplicationInformationHolder();

		Scheduler applicationScheduler = new ConcreteScheduler(applicationName);

		applicationInformationHolder.setApplicationName(applicationName);
		applicationInformationHolder.setApplicationSpecification(applicationSpecification);
		applicationInformationHolder.setApplicationScheduler(applicationScheduler);

		applicationInformationHolders.put(applicationName, applicationInformationHolder);

		applicationInformationHolder.markStart();

		applicationScheduler.prepareApplication();
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
		return launcherMap.values();
	}

	/**
	 * Returns the list of registered launchers, respecting the global user/application placement restrictions,
	 * as well as fine-grained node placement restrictions.
	 * 
	 * @return The list of registered launchers, respecting the security criteria described above.
	 * 
	 * @throws RuntimeGlobalPlacementException If alive launchers exist, but none satisfies global user/application placement restrictions.
	 * @throws RuntimeNodeGroupPlacementException If alive launchers satisfying global security criteria exist, but note satisfies fine-grained
	 *                                            node placement restrictions.
	 */
	public Collection<Launcher> getRegisteredLaunchers(String application, Set<Node> nodes) throws RuntimeGlobalPlacementException, RuntimeNodeGroupPlacementException {
		ApplicationSpecification specification = applicationInformationHolders.get(application).getApplicationSpecification();

		// Filter Launchers that satisfy global user/application restrictions

		String userRestriction = (specification.getUserAuthenticator() != null ? specification.getUserAuthenticator().getEntity() : null);
		String applicationRestriction = (specification.getApplicationAuthenticator() != null ? specification.getApplicationAuthenticator().getEntity() : null);

		List<LauncherInformation> globalFilteredLaunchers = new ArrayList<LauncherInformation>();

		for(LauncherInformation launcherInformation: launcherInformationMap.values()) {
			if(checkGlobalLauncherCompatibility(userRestriction, applicationRestriction, specification.getGlobalRestrictions(), launcherInformation)) {
				globalFilteredLaunchers.add(launcherInformation);
			}
		}

		if(globalFilteredLaunchers.size() == 0 && launcherMap.size() > 0) {
			throw new RuntimeGlobalPlacementException();
		}

		// Filter Launchers that satisfy node restrictions

		List<Launcher> fineFilteredLaunchers = new ArrayList<Launcher>();

		for(LauncherInformation launcherInformation: globalFilteredLaunchers) {
			boolean insert = true;

			for(Node node: nodes) {
				String nodeRestriction = (specification.obtainNodeAuthenticator(node) != null ? specification.obtainNodeAuthenticator(node).getEntity() : null);

				if(!checkNodeLauncherCompatibility(nodeRestriction, specification.obtainNodeRestriction(node), launcherInformation)) {
					insert = false;
					break;
				}
			}

			if(insert) {
				fineFilteredLaunchers.add(launcherMap.get(launcherInformation.getId()));
			}
		}

		if(fineFilteredLaunchers.size() == 0 && launcherMap.size() > 0) {
			throw new RuntimeNodeGroupPlacementException();
		}

		return fineFilteredLaunchers;
	}

	/**
	 * Checks if a Launcher is compatible with global security parameters.
	 * 
	 * @param userRestriction Client identifier.
	 * @param applicationRestriction Application identifier.
	 * @param launcherRestrictions Global security restrictions of the application.
	 * @param launcherInformation Information about the Launcher to be checked.
	 * 
	 * @return True iff the Launcher is compatible with the global security parameters.
	 */
	boolean checkGlobalLauncherCompatibility(String userRestriction, String applicationRestriction, LauncherRestrictions launcherRestrictions, LauncherInformation launcherInformation) {
		boolean useUserSpecificLaunchers = false;
		boolean useApplicationSpecificLaunchers = false;

		CollocationStatus collocationStatus = CollocationStatus.SHARED_OTHERUSER;

		Set<String> launcherIds = new HashSet<String>();

		int freeLauncherSlots = 0;

		// Initiate the restrictions with the global restrictions
		if(launcherRestrictions != null) {
			useUserSpecificLaunchers = launcherRestrictions.isUseUserSpecificLaunchers();
			useApplicationSpecificLaunchers = launcherRestrictions.isUseApplicationSpecificLaunchers();

			collocationStatus = launcherRestrictions.getCollocationStatus();

			launcherIds.addAll(launcherRestrictions.getLauncherIds());

			freeLauncherSlots = launcherRestrictions.getFreeLauncherSlots();
		}

		// If the application has a restriction, it should match the Launcher's
		// If the application does not have a restriction, the Launcher should not have a restriction

		if(useUserSpecificLaunchers && userRestriction != null) {
			if(!launcherInformation.getUserRestrictions().contains(userRestriction)) {
				return false;
			}
		}
		else if(launcherInformation.getUserRestrictions().size() != 0) {
			return false;
		}

		if(useApplicationSpecificLaunchers && applicationRestriction != null) {
			if(!launcherInformation.getApplicationRestrictions().contains(applicationRestriction)) {
				return false;
			}
		}
		else if(launcherInformation.getApplicationRestrictions().size() != 0) {
			return false;
		}

		// If the application request restricted isolation parameters,
		// the Launcher should satisfy them

		switch(collocationStatus) {
		case SHARED_SAMEUSER:
			if(launcherInformation.getColocationStatus() == CollocationStatus.SHARED_OTHERUSER) {
				return false;
			}
			break;
		case ISOLATED:
			if(launcherInformation.getColocationStatus() == CollocationStatus.SHARED_SAMEUSER || launcherInformation.getColocationStatus() == CollocationStatus.SHARED_OTHERUSER) {
				return false;
			}
			break;
		}

		// If the application requested for specific Launchers, disconsider
		// every Launcher different than those specified

		if(launcherIds.size() > 0 && !launcherIds.contains(launcherInformation.getId())) {
			return false;
		}

		// If the application requested some specific baseline of free slots,
		// the Launcher should have that amount of slots available

		if(launcherInformation.getFreeSlots() < freeLauncherSlots) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if a Launcher is compatible with node security parameters.
	 * 
	 * @param nodeRestriction Node identifier.
	 * @param launcherRestrictions Node security restrictions.
	 * @param launcherInformation Information about the Launcher to be checked.
	 * 
	 * @return True iff the Launcher is compatible with the global security parameters.
	 */
	boolean checkNodeLauncherCompatibility(String nodeRestriction, LauncherRestrictions launcherRestrictions, LauncherInformation launcherInformation) {
		boolean useNodeSpecificLaunchers = false;

		CollocationStatus collocationStatus = CollocationStatus.SHARED_OTHERUSER;

		Set<String> launcherIds = new HashSet<String>();

		int freeLauncherSlots = 0;

		// Initiate the restrictions with the global restrictions
		if(launcherRestrictions != null) {
			useNodeSpecificLaunchers = launcherRestrictions.isUseNodeSpecificLaunchers();

			collocationStatus = launcherRestrictions.getCollocationStatus();

			launcherIds.addAll(launcherRestrictions.getLauncherIds());

			freeLauncherSlots = launcherRestrictions.getFreeLauncherSlots();
		}

		// If the application has a restriction, it should match the Launcher's
		// If the application does not have a restriction, the Launcher should not have a restriction

		if(useNodeSpecificLaunchers && nodeRestriction != null) {
			if(!launcherInformation.getNodeRestrictions().contains(nodeRestriction)) {
				return false;
			}
		}
		else if(launcherInformation.getNodeRestrictions().size() != 0) {
			return false;
		}

		// If the application request restricted isolation parameters,
		// the Launcher should satisfy them

		switch(collocationStatus) {
		case SHARED_SAMEUSER:
			if(launcherInformation.getColocationStatus() == CollocationStatus.SHARED_OTHERUSER) {
				return false;
			}
			break;
		case ISOLATED:
			if(launcherInformation.getColocationStatus() == CollocationStatus.SHARED_SAMEUSER || launcherInformation.getColocationStatus() == CollocationStatus.SHARED_OTHERUSER) {
				return false;
			}
			break;
		}

		// If the application requested for specific Launchers, disconsider
		// every Launcher different than those specified

		if(launcherIds.size() > 0 && !launcherIds.contains(launcherInformation.getId())) {
			return false;
		}

		// If the application requested some specific baseline of free slots,
		// the Launcher should have that amount of slots available

		if(launcherInformation.getFreeSlots() < freeLauncherSlots) {
			return false;
		}

		return true;
	}
	
	/**
	 * Returns the requested application information holder.
	 * 
	 * @param application The requested application.
	 * 
	 * @return The requested application information holder.
	 */
	public ApplicationInformationHolder getApplicationInformationHolder(String application) {
		return applicationInformationHolders.get(application);
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
	 * @param arguments Ignored
	 */
	public static void main(String[] arguments) {
		System.out.println("Running " + ConcreteManager.getInstance().toString());
	}
}
