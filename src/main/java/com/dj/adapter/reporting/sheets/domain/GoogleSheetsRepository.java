package com.dj.adapter.reporting.sheets.domain;

import com.dj.adapter.reporting.sheets.retry.AsyncRetryExecutor;
import com.dj.adapter.reporting.sheets.retry.RetryExecutor;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class GoogleSheetsRepository {
	public static Logger logger = LoggerFactory.getLogger(GoogleSheetsRepository.class);
	/**
	 * If non-null, the value; if null, indicates no value is present
	 */
	private static Sheets value;
	private ScheduledExecutorService scheduler;
	private RetryExecutor executor;

	/**
	 * Constructs an empty instance.
	 **/
	public GoogleSheetsRepository() {
		this.value = null;
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		executor = new AsyncRetryExecutor(scheduler).retryOn(IOException.class)
		                                            .withFixedBackoff(200)
		                                            .withFixedRate()
		                                            .withMaxRetries(3);
	}

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 * @throws NullPointerException if value is null
	 */
	public GoogleSheetsRepository(Sheets value) {
		this();
		this.value = Objects.requireNonNull(value);
	}

	/**
	 * Wrapper for Sheets batchGet method, used for fetching multiple ranges of a particular spreadsheet.
	 *
	 * @param spreadSheetId identifier for a particular spreadsheet
	 * @param ranges        ranges to be retrieved
	 * @return values for all ranges
	 */
	public BatchGetValuesResponse getMultipleRanges(String spreadSheetId, List<String> ranges) throws IOException {
		return value.spreadsheets()
		            .values()
		            .batchGet(spreadSheetId)
		            .setRanges(ranges)
		            .execute();
	}

	/**
	 * Wrapper for Sheets get method, used for fetch a range of a particular spreadsheet.
	 *
	 * @param spreadSheetId identifier for a particular spreadsheet
	 * @param range         range to be retrieved
	 * @return value of the range
	 */
	public ValueRange getRange(String spreadSheetId, String range) throws IOException {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		RetryExecutor executor = new AsyncRetryExecutor(scheduler).retryOn(IOException.class)
		                                                          .withFixedBackoff(200)
		                                                          .withFixedRate()
		                                                          .withMaxRetries(3);
		final CompletableFuture<ValueRange> withRetry = executor.getWithRetry(context -> value.spreadsheets()
		                                                                                      .values()
		                                                                                      .get(spreadSheetId, range)
		                                                                                      .execute());
		withRetry.thenAccept(valueRange -> logger.debug("Retrieved values: " + valueRange.toString()));

		return value.spreadsheets()
		            .values()
		            .get(spreadSheetId, range)
		            .execute();
	}

	/**
	 * Wrapper for Sheets append method, used to add a new row at the end of the table
	 * referenced by the range.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param range         range that points to the table where the row will be appended
	 * @param row           values of the cells of the row
	 * @return an append operation ready to be executed
	 */
	public Sheets.Spreadsheets.Values.Append append(String spreadsheetId,
	                                                String range,
	                                                ValueRange row) throws IOException {
		return value.spreadsheets()
		            .values()
		            .append(spreadsheetId, range, row);
	}

	/**
	 * Wrapper for Sheets update method, used to update an existing row with new values.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param range         range to be updated
	 * @param row           value of the cells of the row
	 * @return an update operation ready to be executed
	 */
	public Sheets.Spreadsheets.Values.Update update(String spreadsheetId,
	                                                String range,
	                                                ValueRange row) throws IOException {
		return value.spreadsheets()
		            .values()
		            .update(spreadsheetId, range, row);
	}

	/**
	 * If a value is present in this {@code GoogleSheetsRepository}, returns the value,
	 * otherwise throws {@code NoSuchElementException}.
	 *
	 * @return the non-null value held by this {@code GoogleSheetsRepository}
	 * @throws NoSuchElementException if there is no value present
	 * @see GoogleSheetsRepository#isPresent()
	 */
	public Sheets get() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * Return {@code true} if there is a value present, otherwise {@code false}.
	 *
	 * @return {@code true} if there is a value present, otherwise {@code false}
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * If a valid spreadsheet with a given identifier exists, return the spreadsheet,
	 * otherwise, return an empty {@code SpreadSheet}
	 *
	 * @param spreadSheetId
	 * @return
	 * @throws IOException
	 */
	public GoogleSpreadsheet getSpreadSheetById(String spreadSheetId) throws IOException {
		return new GoogleSpreadsheet(value.spreadsheets()
		                                  .get(spreadSheetId)
		                                  .execute(), this);
	}
}
