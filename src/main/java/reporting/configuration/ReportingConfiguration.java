package reporting.configuration;


import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reporting.domain.*;
import reporting.retry.AsyncRetryExecutor;
import reporting.service.ReportingPoliciesService;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAutoConfiguration
public class ReportingConfiguration {
	private static final Integer EXECUTOR_MAX_RETRIES = 15;
	private static final Integer EXECUTOR_BACKOFF_EXP_INITIAL_DELAY = 2000;
	private static final Double EXECUTOR_BACKOFF_EXP_MULTIPLIER = 1.5;

	@Value("${google.pkey_path}")
	private String googlePKeyPath;

	@Bean
	ScheduledExecutorService scheduledExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Bean
	AsyncRetryExecutor asyncRetryExecutor() {
		return new AsyncRetryExecutor(scheduledExecutorService()).withExponentialBackoff(EXECUTOR_BACKOFF_EXP_INITIAL_DELAY,
		                                                                                 EXECUTOR_BACKOFF_EXP_MULTIPLIER)
		                                                         .withMaxRetries(EXECUTOR_MAX_RETRIES)
		                                                         .retryOn(GoogleJsonResponseException.class,
		                                                                  IOException.class);
	}

	@Bean
	GoogleSheetsRepositoryFactory repositoryFactory() {
		return new GoogleSheetsRepositoryFactory(googlePKeyPath);
	}

	@Bean
	GoogleSheetsRepository sheetsRepository(GoogleSheetsRepositoryFactory repositoryFactory) {
		return repositoryFactory.getRepository()
		                        .withRetryExecutor(asyncRetryExecutor());
	}

	@Bean
	FormattingPolicy formattingPolicy() {
		return new FormattingPolicy().addFormatters(ReportingPoliciesService::metaVariableForCurrentDateFormat,
		                                            ReportingPoliciesService::dateToStringFormat);
	}

	@Bean
	ValidationPolicy validationPolicy() {
		return new ValidationPolicy().addValidators(ReportingPoliciesService::checkForNullValues,
		                                            ReportingPoliciesService::checkMaximumLength);
	}

	@Bean
	RowTemplate rowTemplate() {
		return new RowTemplate(validationPolicy(), formattingPolicy());
	}
}
