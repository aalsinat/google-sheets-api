package com.dj.adapter.reporting.sheets.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FormattingPolicy {
	public static final FormattingPolicy DEFAULT = new FormattingPolicy();

	private final Set<Formatter> formatters;

	public FormattingPolicy() {
		this(Collections.emptySet());
	}

	public FormattingPolicy(Set<Formatter> applyFormat) {
		this.formatters = applyFormat;
	}

	private static <T> Set<T> setPlusElems(Set<T> initial, T... newElement) {
		final HashSet<T> copy = new HashSet<>(initial);
		copy.addAll(Arrays.asList(newElement));
		return Collections.unmodifiableSet(copy);
	}

	public FormattingPolicy applyFormat(Formatter... applyFormats) {
		return new FormattingPolicy(setPlusElems(formatters, applyFormats));
	}

	public Set<Formatter> getFormatters() {
		return formatters;
	}
}
