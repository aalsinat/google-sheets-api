package reporting.domain;

import reporting.service.ReportingPoliciesService;
import org.springframework.beans.factory.annotation.Autowired;

public class RowTemplate {
	private final ValidationPolicy validationPolicy;
	private final FormattingPolicy formattingPolicy;

	@Autowired
	public RowTemplate(ValidationPolicy validationPolicy, FormattingPolicy formattingPolicy) {
		this.validationPolicy = validationPolicy;
		this.formattingPolicy = formattingPolicy;
	}

	public Row build() {
		return new Row.RowBuilder().withValidationPolicy(validationPolicy)
		                           .withFormattingPolicy(formattingPolicy)
		                           .withAdditionPolicy(ReportingPoliciesService::isMetaVariableValue)
		                           .build();
	}
}
