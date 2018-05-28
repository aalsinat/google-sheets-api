package com.dj.adapter.reporting.sheets.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ValidationPolicy {
	public static final ValidationPolicy DEFAULT = new ValidationPolicy();

	private final Set<Validator> validators;

	public ValidationPolicy() {
		this(Collections.emptySet());
	}

	public ValidationPolicy(Set<Validator> applyValidation) {
		this.validators = applyValidation;
	}

	private static <T> Set<T> setPlusElems(Set<T> initial, T... newElement) {
		final HashSet<T> copy = new HashSet<>(initial);
		copy.addAll(Arrays.asList(newElement));
		return Collections.unmodifiableSet(copy);
	}

	public ValidationPolicy applyValidation(Validator... applyValidators) {
		return new ValidationPolicy(setPlusElems(validators, applyValidators));
	}

	public Set<Validator> getValidators() {
		return validators;
	}
}
