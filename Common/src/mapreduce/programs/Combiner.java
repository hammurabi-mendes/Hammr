package mapreduce.programs;

import java.util.Set;

import java.util.Map;
import java.util.HashMap;

public abstract class Combiner<O,V> {
	private static final long serialVersionUID = 1L;

	private Map<O,V> currentValues;

	public Combiner() {
		currentValues = new HashMap<O,V>();
	}

	public void add(O object, V newValue) {
		V updatedValue;

		V oldValue = currentValues.get(object);

		if(oldValue != null) {
			updatedValue = combine(oldValue, newValue);
		}
		else {
			updatedValue = newValue;
		}

		currentValues.put(object, updatedValue);
	}

	public Set<Map.Entry<O,V>> getCurrentValues() {
		return currentValues.entrySet();
	}

	public abstract V combine(V oldValue, V newValue);
}
