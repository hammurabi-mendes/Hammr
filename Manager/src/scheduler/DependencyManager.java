package scheduler;

import java.util.Set;
import java.util.Map;

import java.util.HashSet;
import java.util.HashMap;

public class DependencyManager<X, Y> {
	private Map<X, Set<Y>> dependencies;

	private Map<Y, Integer> lockedDependents;

	private Set<Y> freeDependents;

	public DependencyManager() {
		dependencies = new HashMap<X, Set<Y>>();

		lockedDependents = new HashMap<Y, Integer>();

		freeDependents = new HashSet<Y>();
	}

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

	public boolean hasFreeDependents() {
		return (freeDependents.size() > 0);
	}

	public boolean hasLockedDependents() {
		return (lockedDependents.size() > 0);
	}

	public Set<Y> obtainFreeDependents() {
		Set<Y> result = freeDependents;

		resetFreeDependents();

		return result;
	}

	public void resetFreeDependents() {
		freeDependents = new HashSet<Y>();
	}

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
