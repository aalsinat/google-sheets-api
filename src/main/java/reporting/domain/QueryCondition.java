package reporting.domain;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class QueryCondition<K, V> {
	private final Map.Entry<K, V> clause;

	public QueryCondition(K field, V value) {
		this.clause = new AbstractMap.SimpleEntry<>(field, value);
	}

	/**
	 * Returns a list of A1 notations referring to clause keys.
	 * This function will be very coupled to header entity.
	 *
	 * @return
	 */
	public List<String> getRanges() {
		return null;
	}


}
