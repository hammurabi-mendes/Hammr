package scheduler;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import appspecs.ApplicationSpecification;

/**
 * Interface that represents a scheduler.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public interface Scheduler {
	/**
	 * Try to schedule the next wave of NodeGroups: NodeGroupBundles are NodeGroups that should
	 * be schedule at the same time.
	 * 
	 * @return False if no NodeGroupBundle is available to execution; true otherwise.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the next wave of NodeGroups.
	 */
	public boolean scheduleNodeGroupBundle() throws InsufficientLaunchersException;
	
	/**
	 * Informs the scheduler a particular NodeGroup has finished its execution.
	 * 
	 * @param serialNumber The serial number of the NodeGroup that has finished its execution.
	 * 
	 * @return True if this is the first termination notification for this NodeGroup; false otherwise. The current
	 * scheduler implementation only has one possible termination notification, since it doesn't handle failures.
	 */
	public boolean handleTermination(Long serialNumber);

	/**
	 * Setups the scheduler for the new application being executed.
	 * @param applicationSpecification Application specification.
	 * 
	 * @return True if the setup finished successfully; false otherwise.
	 * 
	 * @throws TemporalDependencyException If the application specification has a temporal dependency problem.
	 * @throws CyclicDependencyException If the application specification has a cyclic dependency problem.
	 */
	public boolean setup(ApplicationSpecification applicationSpecification) throws TemporalDependencyException, CyclicDependencyException;
	
	/**
	 * Tests whether all the Node/NodeGroups were already executed.
	 * 
	 * @return True if all the Node/NodeGroups were already executed, false otherwise.
	 */
	public boolean finished();
}
