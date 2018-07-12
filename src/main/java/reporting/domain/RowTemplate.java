package reporting.domain;

import org.springframework.beans.factory.annotation.Autowired;
import reporting.service.ReportingPoliciesService;

public class RowTemplate {
	private final ValidationPolicy validationPolicy;
	private final FormattingPolicy formattingPolicy;

	@Autowired
	public RowTemplate(ValidationPolicy validationPolicy, FormattingPolicy formattingPolicy) {
		this.validationPolicy = validationPolicy;
		this.formattingPolicy = formattingPolicy;
	}

	public Row build() {
		return Row.builder().withValidationPolicy(validationPolicy)
		          .withFormattingPolicy(formattingPolicy)
		          .withAdditionPolicy(ReportingPoliciesService::isMetaVariableValue)
		          .build();
	}
}
