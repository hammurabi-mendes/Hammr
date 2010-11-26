package scheduler;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import appspecs.ApplicationSpecification;

public interface Scheduler {
	public boolean scheduleNodeGroupBundle() throws InsufficientLaunchersException;
	public boolean handleTermination(Long serialNumber);

	public boolean setup(ApplicationSpecification applicationSpecification) throws TemporalDependencyException, CyclicDependencyException;
	public boolean finished();
}
