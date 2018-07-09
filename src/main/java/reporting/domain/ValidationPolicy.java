package reporting.domain;

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

	public ValidationPolicy addValidators(Validator... validatorsList) {
		return new ValidationPolicy(setPlusElems(validators, validatorsList));
	}

	public Set<Validator> getValidators() {
		return validators;
	}

	public Object apply(String key, Object value) {
		Object current = value;
		for (Validator validator : validators) current = validator.validate(key, current);
		return current;
	}

}
