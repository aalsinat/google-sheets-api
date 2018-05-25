package com.dj.adapter.reporting.sheets.domain;

import java.util.AbstractMap;
import java.util.Map;

public class QueryCondition<K, V> {
	private final Map.Entry<K, V> clause;

	public QueryCondition(K field, V value) {
		this.clause = new AbstractMap.SimpleEntry<>(field, value);
	}
}
