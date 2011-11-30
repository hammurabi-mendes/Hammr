/*
Copyright (c) 2011, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package scheduler;

import java.util.Map;

import execinfo.aggregators.Aggregator;

import exceptions.InsufficientLaunchersException;
import exceptions.TemporalDependencyException;
import exceptions.CyclicDependencyException;

import exceptions.InexistentInputException;
import exceptions.InexistentOutputException;

/**
 * Interface that represents a scheduler.
 * 
 * @author Hammurabi Mendes (hmendes)
 */
public interface Scheduler {
	/**
	 * Setups the scheduler for the new application being executed.
	 * 
	 * @throws TemporalDependencyException If the application specification has a temporal dependency problem.
	 * @throws CyclicDependencyException If the application specification has a cyclic dependency problem.
	 */
	public void prepareApplication() throws TemporalDependencyException, CyclicDependencyException;

	/**
	 * Terminates the application .
	 */
	public void terminateApplication();

	/**
	 * Tests whether the application has finished.
	 * 
	 * @param aggregatedVariables The variables aggregated in the iteration
	 * 
	 * @return True if the application has finished, false otherwise.
	 */
	public boolean finishedApplication(Map<String, Aggregator<? extends Object>> aggregatedVariables);

	/**
	 * Prepare an iteration for the application.
	 * 
	 * @throws InexistentInputException If one of the inputs are missing.
	 */
	public void prepareIteration() throws InexistentInputException;

	/**
	 * Terminates the iteration .
	 * 
	 * @throws InexistentOutputException If one of the outputs are missing.
	 */
	public void terminateIteration() throws InexistentOutputException;

	/**
	 * Tests whether all the Node/NodeGroups were already executed for this iteration.
	 * 
	 * @return True if all the Node/NodeGroups were already executed for this iteration, false otherwise.
	 */
	public boolean finishedIteration();

	/**
	 * Try to schedule the next wave of NodeGroups: NodeGroupBundles are NodeGroups that should
	 * be schedule at the same time.
	 * 
	 * @return False if no NodeGroupBundle is available to execution; true otherwise.
	 * 
	 * @throws InsufficientLaunchersException If no alive Launcher can receive the next wave of NodeGroups.
	 */
	public boolean schedule() throws InsufficientLaunchersException;

	/**
	 * Informs the scheduler a particular NodeGroup has finished its execution.
	 * 
	 * @param serialNumber The serial number of the NodeGroup that has finished its execution.
	 * 
	 * @return True if this is the first termination notification for this NodeGroup; false otherwise. The current
	 * scheduler implementation only has one possible termination notification, since it doesn't handle failures.
	 */
	public boolean handleTermination(Long serialNumber);
}
