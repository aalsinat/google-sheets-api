package reporting.domain;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporting.retry.RetryExecutor;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class GoogleSheetsRepository {
	public static Logger logger = LoggerFactory.getLogger(GoogleSheetsRepository.class);
	/**
	 * If non-null, the value; if null, indicates no value is present
	 */
	private static Sheets value;
	private RetryExecutor executor;

	/**
	 * Constructs an empty instance.
	 **/
	public GoogleSheetsRepository() {
		this.value = null;

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
	 * If set, returns a {@link RetryExecutor} instance, otherwise null.
	 *
	 * @return a particular {@code RetryExecutor} instance associated with current repository
	 */
	public RetryExecutor getExecutor() {
		return executor;
	}

	/**
	 * Adds a retry executor to the repository.
	 *
	 * @param executor instance of a particular {@link RetryExecutor} to be set
	 */
	public GoogleSheetsRepository withRetryExecutor(RetryExecutor executor) {
		this.executor = executor;
		return this;
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
	 * Asynchronous and retry version of {@code getMultipleRanges} method.
	 *
	 * @param spreadSheetId identifier for a particular spreadsheet
	 * @param ranges        ranges to be retrieved
	 * @return values for all ranges
	 */
	public CompletableFuture<BatchGetValuesResponse> getMultipleRangesWithRetry(String spreadSheetId,
	                                                                            List<String> ranges) throws IOException {
		return executor.getWithRetry(context -> {
			logger.debug("Getting multiple ranges API method. Attempt #{}.", context.getRetryCount() + 1);
			return getMultipleRanges(spreadSheetId, ranges);
		});
	}

	/**
	 * Wrapper for Sheets get method, used for fetch a range of a particular spreadsheet.
	 *
	 * @param spreadSheetId identifier for a particular spreadsheet
	 * @param range         range to be retrieved
	 * @return value of the range
	 */
	public ValueRange getRange(String spreadSheetId, String range) throws IOException {
		return value.spreadsheets()
		            .values()
		            .get(spreadSheetId, range)
		            .execute();
	}

	/**
	 * Asynchronous and retry version of {@code getRange} method.
	 *
	 * @param spreadSheetId identifier for a particular spreadsheet
	 * @param range         range to be retrieved
	 * @return value of the range
	 */
	public CompletableFuture<ValueRange> getRangeWithRetry(String spreadSheetId, String range) throws IOException {
		return executor.getWithRetry(context -> {
			logger.debug("Getting range API method. Attempt #{}.", context.getRetryCount() + 1);
			return getRange(spreadSheetId, range);
		});
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
	public AppendValuesResponse append(String spreadsheetId,
	                                   String range,
	                                   ValueRange row) throws IOException {
		return value.spreadsheets()
		            .values()
		            .append(spreadsheetId, range, row)
		            .setValueInputOption("USER_ENTERED")
		            .setInsertDataOption("INSERT_ROWS")
		            .setResponseDateTimeRenderOption(
				            "FORMATTED_STRING")
		            .setIncludeValuesInResponse(true)
		            .execute();
	}

	/**
	 * Asynchronous and retry version of {@code append} method.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param range         range that points to the table where the row will be appended
	 * @param row           values of the cells of the row
	 * @return
	 * @throws IOException
	 */
	public CompletableFuture<AppendValuesResponse> appendWithRetry(String spreadsheetId,
	                                                               String range,
	                                                               ValueRange row) throws IOException {
		return executor.getWithRetry(context -> {
			logger.debug("Append API method with values {} to range {}. Attempt #{}.", row.getValues(), range, context.getRetryCount() + 1);
			return append(spreadsheetId, range, row);
		});
	}

	/**
	 * Wrapper for Sheets batchUpdate method, used to set values in one or more ranges of a spreadsheet.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param request       request body containing how the input should be interpreted, the new values to apply to
	 *                      the spreadsheet, whether the update response should include the values of the cells that
	 *                      will be updated, etc.
	 * @return
	 */
	public Sheets.Spreadsheets.Values.BatchUpdate batchUpdate(String spreadsheetId,
	                                                          BatchUpdateValuesRequest request) throws IOException {
		return value.spreadsheets()
		            .values()
		            .batchUpdate(spreadsheetId, request);
	}


	/**
	 * Wrapper for Sheets update method, used to update an existing row with new values.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param range         range to be updated
	 * @param row           value of the cells of the row
	 * @return response object from Google Sheets API request
	 */
	public UpdateValuesResponse update(String spreadsheetId, String range, ValueRange row) throws IOException {
		return value.spreadsheets()
		            .values()
		            .update(spreadsheetId, range, row)
		            .setIncludeValuesInResponse(true)
		            .setValueInputOption("USER_ENTERED")
		            .setResponseDateTimeRenderOption(
				            "FORMATTED_STRING")
		            .execute();
	}

	/**
	 * Asynchronous and retry version of {@code update} method.
	 *
	 * @param spreadsheetId identifier for a particular spreadsheet
	 * @param range         range to be updated
	 * @param row           value of the cells of the row
	 * @return an update operation ready to be executed
	 */
	public CompletableFuture<UpdateValuesResponse> updateWithRetry(String spreadsheetId, String range,
	                                                               ValueRange row) throws IOException {
		return executor.getWithRetry(context -> {
			logger.debug("Update API method with values {}. Attempt #{}.", row.getValues(), context.getRetryCount() + 1);
			return update(spreadsheetId, range, row);
		});
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
	 * @param spreadSheetId identifier of the {@code SpreadSheet} to be found
	 * @return an instance that represents the {@code SpreadSheet} having the given identifier
	 */
	public GoogleSpreadsheet getSpreadSheetById(String spreadSheetId) throws IOException {
		return new GoogleSpreadsheet(value.spreadsheets()
		                                  .get(spreadSheetId)
		                                  .execute(), this);
	}
}
