package com.dj.adapter.reporting.sheets.configuration;

import com.dj.adapter.reporting.sheets.configuration.reports.FirstSheetConfiguration;
import com.dj.adapter.reporting.sheets.domain.GoogleSheet;
import com.dj.adapter.reporting.sheets.domain.GoogleSheetsRepository;
import com.dj.adapter.reporting.sheets.domain.GoogleSheetsRepositoryFactory;
import com.dj.adapter.reporting.sheets.retry.AsyncRetryExecutor;
import com.dj.adapter.reporting.sheets.retry.RetryExecutor;
import com.dj.adapter.reporting.sheets.service.SheetsReportingService;
import com.google.api.client.util.Value;
import com.google.api.services.sheets.v4.model.GridRange;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAutoConfiguration
public class ReportingConfiguration {

	@Value("${google.reporting.retry-policy.max-retries}")
	int maxRetries;

	@Value("${google.reporting.retry-policy.fixed-backoff}")
	int fixedBackoff;

	@Bean(destroyMethod = "shutdownNow")
	public ScheduledExecutorService scheduler() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Bean
	public RetryExecutor retryExecutor() {
		return new AsyncRetryExecutor(scheduler()).withFixedBackoff(fixedBackoff)
		                                          .withFixedRate()
		                                          .withMaxRetries(maxRetries);
	}

	@Bean
	GoogleSheetsRepositoryFactory repositoryFactory() {
		return new GoogleSheetsRepositoryFactory();
	}

	@Bean
	GoogleSheetsRepository sheetsRepository(GoogleSheetsRepositoryFactory repositoryFactory) {
		return repositoryFactory.getRepository("client_secret.json");
	}

	@Bean
	SheetsReportingService sheetsReportingService(GoogleSheetsRepository sheetsRepository) {
		return new SheetsReportingService(sheetsRepository);
	}

	@Bean
	GoogleSheet firstSheet(SheetsReportingService reportingService,
	                       FirstSheetConfiguration firstSheetConfiguration) throws IOException {
		FirstSheetConfiguration.HeaderOffset configuredHeaderOffset = firstSheetConfiguration.getHeaderOffset();
		GridRange sheetHeaderOffset = new GridRange().setStartRowIndex(configuredHeaderOffset.getStartRowIndex())
		                                             .setEndRowIndex(configuredHeaderOffset.getEndRowIndex())
		                                             .setStartColumnIndex(configuredHeaderOffset.getStartColumnIndex())
		                                             .setEndColumnIndex(configuredHeaderOffset.getEndColumnIndex());
		return reportingService.getSheetByName(firstSheetConfiguration.getSpreadsheetId(),
		                                       firstSheetConfiguration.getSheetName())
		                       .setHeaderOffset(sheetHeaderOffset);

	}
}
