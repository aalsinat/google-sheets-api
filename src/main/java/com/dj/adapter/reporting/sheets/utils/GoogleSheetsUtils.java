package com.dj.adapter.reporting.sheets.utils;

import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class GoogleSheetsUtils {
	/**
	 * Merges the result of reading multiple ranges.
	 *
	 * @param response The response when retrieving more than one range of values in a spreadsheet.
	 * @return a list containing values for every range
	 */
	public static MergedValueRanges mergeMultipleValueRanges(BatchGetValuesResponse response) {
		return MergedValueRanges.of(response.getValueRanges()
		                                    .stream()
		                                    .map(valueRange -> valueRange.getValues())
		                                    .collect(Collectors.toList()));
	}

	/**
	 * Given the position of a column, it returns its name within the spreadsheet header.
	 *
	 * @param map   match the column names in the header to their position on the sheet
	 * @param value position name to be found
	 * @return the column name of that position or empty()
	 */
	public static <T, E> Optional<T> getHeaderColumnNameByPosition(Map<T, E> map, E value) {
		return map.entrySet()
		          .stream()
		          .filter(entry -> Objects.equals(entry.getValue(), value))
		          .map(Map.Entry::getKey)
		          .findAny();
	}

	/**
	 * Given columns and values of a row, return the value corresponding to columnName if is present,
	 * otherwise defaultValue is returned.
	 *
	 * @param row          map of columns and values of a row
	 * @param columnName   the name of the column to be fetched
	 * @param defaultValue the way to get a default value
	 * @return value of found column if it is present or default value
	 */
	public static <T, E> E getColumnValueForRow(Map<T, E> row, T columnName, Supplier<E> defaultValue) {
		return row.containsKey(columnName) ? row.get(columnName) : defaultValue.get();
	}

	/**
	 * Converts a list of objects to a map where the value is object's postion on the list
	 *
	 * @param items list of objects to be transformed
	 * @return a map for postions in the list
	 */
	public static Map<String, Integer> listToMap(List<Object> items, Integer offset) {
		final Map<String, Integer> result = new HashMap<>();
		items.stream()
		     .forEach(item -> result.put(String.valueOf(item), items.indexOf(item) + offset));
		return result;
	}

}
