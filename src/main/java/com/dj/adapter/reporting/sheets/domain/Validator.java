package com.dj.adapter.reporting.sheets.domain;

@FunctionalInterface
public interface Validator {
	/**
	 * Applies this function to the given arguments.
	 *
	 * @param k the first function argument
	 * @param v the second function argument
	 * @return the function result
	 */
	Object validate(String k, Object v);
}
