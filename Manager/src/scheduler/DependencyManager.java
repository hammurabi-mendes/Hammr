package scheduler;

import java.util.Set;
import java.util.Map;

import java.util.HashSet;
import java.util.HashMap;

/**
 * This class manages the dependencies between generic items of possibly different types.
 * 
 * @author Hammurabi Mendes (hmendes)
 *
 * @param <X> Type of the first item.
 * @param <Y> Type of the second item.
 */
public class DependencyManager<X, Y> {
	// Maps trigger -> Its own dependencies
	private Map<X, Set<Y>> dependencies;

	// Counts the number of triggerers still present for each dependency
	private Map<Y, Integer> lockedDependents;

	// Dependents that don't have any present triggerer
	private Set<Y> freeDependents;

	/**
	 * Constructor method.
	 */
	public DependencyManager() {
		dependencies = new HashMap<X, Set<Y>>();

		lockedDependents = new HashMap<Y, Integer>();

		freeDependents = new HashSet<Y>();
	}

	/**
	 * Informs a new relation triggerer / dependent. The dependent is only released
	 * when all of its triggeres are removed.
	 * 
	 * @param triggerer The second parameter depends on this parameter.
	 * @param dependent This parameter is dependent of the first parameter.
	 */
	public void insertDependency(X triggerer, Y dependent) {
		if(triggerer == null) {
			freeDependents.add(dependent);

			return;
		}

		Set<Y> currentDependents;

		if(dependencies.containsKey(triggerer)) {
			currentDependents = dependencies.get(triggerer);
		}
		else {
			currentDependents = new HashSet<Y>();
		}

		if(currentDependents.contains(dependent)) {
			return;
		}

		currentDependents.add(dependent);

		dependencies.put(triggerer, currentDependents);

		increaseCounter(dependent);
	}

	/**
	 * Removes a triggerer.
	 * 
	 * @param triggerer Triggerer to be removed.
	 */
	public void removeDependency(X triggerer) {
		Set<Y> currentDependents;

		if(dependencies.containsKey(triggerer)) {
			currentDependents = dependencies.get(triggerer);

			for(Y currentDependent: currentDependents) {
				decreaseCounter(currentDependent);
			}

			dependencies.remove(triggerer);
		}
	}

	/**
	 * Returns true iff some dependents don't have any present triggerer
	 * 
	 * @return True if some dependents don't have any present triggerer; false otherwise.
	 */
	public boolean hasFreeDependents() {
		return (freeDependents.size() > 0);
	}

	/**
	 * Returns true iff some dependents do have any present triggerer
	 * 
	 * @return True if some dependents do have any present triggerer; false otherwise.
	 */
	public boolean hasLockedDependents() {
		return (lockedDependents.size() > 0);
	}

	/**
	 * Obtains the dependents that don't have any present triggerer, effectively "releasing" them.
	 * 
	 * @return The dependents that don't have any present triggerer.
	 */
	public Set<Y> obtainFreeDependents() {
		Set<Y> result = freeDependents;

		resetFreeDependents();

		return result;
	}

	/**
	 * Reset the list of free dependencies after some dependents are effectively released.
	 */
	public void resetFreeDependents() {
		freeDependents = new HashSet<Y>();
	}

	/**
	 * Increases the number of triggerers for a particular dependent.
	 * 
	 * @param dependent Dependent that has its number of triggerers increased.
	 * 
	 * @return The new number of triggerers for a particular dependent.
	 */
	private int increaseCounter(Y dependent) {
		int currentCounter;

		if(lockedDependents.containsKey(dependent)) {
			currentCounter = lockedDependents.get(dependent);
		}
		else {
			currentCounter = 0;
		}

		currentCounter++;

		lockedDependents.put(dependent, currentCounter);

		return currentCounter;
	}

	/**
	 * Decrease the number of triggerers for a particular dependent.
	 * 
	 * @param dependent Dependent that has its number of triggerers decreased.
	 * 
	 * @return The new number of triggerers for a particular dependent.
	 */
	private int decreaseCounter(Y dependent) {
		int currentCounter;

		if(lockedDependents.containsKey(dependent)) {
			currentCounter = lockedDependents.get(dependent);

			currentCounter--;

			if(currentCounter <= 0) {
				lockedDependents.remove(dependent);

				freeDependents.add(dependent);
			}
			else {
				lockedDependents.put(dependent, currentCounter);
			}

			return currentCounter;
		}

		assert false;

		return -1;
	}
}
