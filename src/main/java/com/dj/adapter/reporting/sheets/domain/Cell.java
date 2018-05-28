package com.dj.adapter.reporting.sheets.domain;

import java.util.AbstractMap;
import java.util.Map;

public class Cell<String, Object> {
	private final Map.Entry<String, Object> value;

	public Cell(String column, Object value) {
		this.value = new AbstractMap.SimpleEntry<>(column, value);
	}

	public Cell(Map.Entry<String, Object> value) {
		this.value = value;
	}

	public String getColumn() {
		return value.getKey();
	}

	public Object getValue() {
		return value.getValue();
	}

}
