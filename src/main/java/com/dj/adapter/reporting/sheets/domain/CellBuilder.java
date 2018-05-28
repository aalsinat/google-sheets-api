package com.dj.adapter.reporting.sheets.domain;

import java.util.Optional;

public class CellBuilder {
	private final FormattingPolicy formattingPolicy;
	private final ValidationPolicy validationPolicy;

	public CellBuilder() {
		this.formattingPolicy = FormattingPolicy.DEFAULT;
		this.validationPolicy = ValidationPolicy.DEFAULT;
	}

	public CellBuilder(FormattingPolicy formattingPolicy) {
		this.formattingPolicy = formattingPolicy;
		this.validationPolicy = ValidationPolicy.DEFAULT;

	}

	public CellBuilder(ValidationPolicy validationPolicy) {
		this.formattingPolicy = FormattingPolicy.DEFAULT;
		this.validationPolicy = validationPolicy;
	}

	public CellBuilder(FormattingPolicy formattingPolicy, ValidationPolicy validationPolicy) {
		this.formattingPolicy = formattingPolicy;
		this.validationPolicy = validationPolicy;
	}

	public CellBuilder withFormattingPolicy(FormattingPolicy formattingPolicy) {
		return new CellBuilder(formattingPolicy, validationPolicy);
	}

	public Cell create(String column, Object value) {
		Optional<Object> validated = validationPolicy.getValidators()
		                                       .stream()
		                                       .map(validator -> validator.validate(column, value))
		                                       .findAny();
		Optional<Object> formattedValue = formattingPolicy.getFormatters()
		                                                  .stream()
		                                                  .map(formatter -> formatter.format(column, validated))
		                                                  .findAny();
		return new Cell(column, formattedValue.get());
	}
}
