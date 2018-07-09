package reporting.domain;

import java.util.*;

public class FormattingPolicy {
	public static final FormattingPolicy DEFAULT = new FormattingPolicy();

	private final List<Formatter> formatters;

	public FormattingPolicy() {
		this(Collections.emptyList());
	}

	public FormattingPolicy(List<Formatter> applyFormat) {
		this.formatters = applyFormat;
	}

	private static <T> List<T> setPlusElems(List<T> initial, T... newElement) {
		final List<T> copy = new ArrayList<>(initial);
		copy.addAll(Arrays.asList(newElement));
		return Collections.unmodifiableList(copy);
	}

	public FormattingPolicy addFormatters(Formatter... formattersList) {
		return new FormattingPolicy(setPlusElems(formatters, formattersList));
	}

	public List<Formatter> getFormatters() {
		return formatters;
	}

	public Object apply(String key, Object value) {
		Object current = value;
		for (Formatter formatter : formatters) current = formatter.format(key, current);
		return current;
	}
}
