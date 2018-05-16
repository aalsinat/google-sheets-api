package com.dj.adapter.reporting.sheets.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MergedValueRanges {
	private final List<List<List<Object>>> value;

	private MergedValueRanges(List<List<List<Object>>> value) {
		this.value = value;
	}

	/**
	 * Returns an {@code MergedValueRanges} with the specified present non-null value.
	 *
	 * @param value a list containing values for every range
	 * @return a {@code MergedValueRanges} with the value present
	 */
	public static MergedValueRanges of(List<List<List<Object>>> value) {
		return new MergedValueRanges(value);
	}

	/**
	 * Checks the minimum size of the merged lists.
	 *
	 * @return the size of the list with fewer elements
	 */
	private Optional<Integer> minimumSize() {
		return this.value.stream().map(List::size).min(Integer::compareTo);
	}

	/**
	 * Concatenate all the elements, item by item, from the different lists, using provided delimiter.
	 *
	 * @param delimiter character sequence used as delimiter
	 * @return a unique list of concatenated elements
	 */
	public Optional<List<String>> concat(CharSequence delimiter) {
		return Optional.ofNullable(IntStream.range(1, this.minimumSize().get())
		                                    .mapToObj(index -> this.value.stream()
		                                                                 .map(value -> value.get(index))
		                                                                 .map(item -> String.valueOf(item.stream()
		                                                                                                 .findFirst()
		                                                                                                 .get()))
		                                                                 .collect(Collectors.joining(delimiter)))
		                                    .collect(Collectors.toList()));
	}

}
