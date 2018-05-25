package com.dj.adapter.reporting.sheets.domain;


import com.dj.adapter.reporting.sheets.retry.AsyncRetryExecutor;
import com.dj.adapter.reporting.sheets.retry.RetryExecutor;
import com.dj.adapter.reporting.sheets.utils.A1NotationHelper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dj.adapter.reporting.sheets.utils.A1NotationHelper.getNotationFromSheetNameAndGridRange;
import static com.dj.adapter.reporting.sheets.utils.GoogleSheetsUtils.*;

public class GoogleSheet {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(GoogleSheet.class);
	private final Sheet value;
	private final GoogleSheetsRepository repository;
	private ScheduledExecutorService scheduler;
	private RetryExecutor executor;
	private String spreadSheetId;
	private GridRange headerOffset;
	private boolean refreshHeaderColumns;

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 * @throws NullPointerException if value is null
	 */
	@Autowired
	public GoogleSheet(Sheet value, GoogleSheetsRepository repository) {
		logger.debug("A new instance of GoogleSheet with id {} and title '{}' is being created. ",
		             value.getProperties()
		                  .getSheetId(),
		             value.getProperties()
		                  .getTitle());
		this.value = Objects.requireNonNull(value);
		this.repository = repository;
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		executor = new AsyncRetryExecutor(scheduler).withFixedBackoff(200)
		                                            .withFixedRate()
		                                            .withMaxRetries(3);
		headerOffset = new GridRange().setSheetId(value.getProperties()
		                                               .getSheetId())
		                              .setStartColumnIndex(0)
		                              .setStartRowIndex(0);
		refreshHeaderColumns = false;
	}

	public String getSpreadSheetId() {
		return spreadSheetId;
	}

	public GoogleSheet setSpreadSheetId(String spreadSheetId) {
		this.spreadSheetId = spreadSheetId;
		return this;
	}

	public void setRefreshHeaderColumns(boolean refreshHeaderColumns) {
		this.refreshHeaderColumns = refreshHeaderColumns;
	}

	/**
	 * If a value is present in this {@code GoogleSheet}.
	 *
	 * @return the non-null value held by this {@code GoogleSheetsRepository}
	 * @throws NoSuchElementException if there is no value present
	 * @see GoogleSheet#isPresent()
	 */
	public Sheet get() {
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
	 * The ID of the sheet. Must be non-negative.
	 *
	 * @return value for the identifier of actual sheet
	 */
	public Integer getSheetId() {
		if (this.isPresent()) return value.getProperties()
		                                  .getSheetId();
		throw new NullPointerException("getSheetId: Google sheet is empty, so there is no identifier to be returned.");
	}

	/**
	 * The title of the sheet.
	 *
	 * @return value for the title of actual sheet
	 */
	public String getSheetTitle() {
		if (this.isPresent()) return value.getProperties()
		                                  .getTitle();
		throw new NullPointerException("getSheetTitle: Google sheet is empty, so there is no title to be returned.");
	}

	/**
	 * The number of rows in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getRowCount() {
		return (isPresent()) ?
		       Optional.of(value.getProperties()
		                        .getGridProperties()
		                        .getRowCount()) :
		       Optional.empty();
	}

	/**
	 * The number of columns in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getColumnCount() {
		return (isPresent()) ?
		       Optional.of(value.getProperties()
		                        .getGridProperties()
		                        .getColumnCount()) :
		       Optional.empty();
	}

	/**
	 * Get current header values, based on the value of the range
	 * set for the header.
	 *
	 * @return
	 */
	public Map<String, Integer> getHeader() throws IOException {
		final String headerRange = getNotationFromSheetNameAndGridRange(this.getSheetTitle(),
		                                                                this.getHeaderOffset());
		final Optional<List<Object>> headerTitles = repository.getRange(spreadSheetId, headerRange)
		                                                      .getValues()
		                                                      .stream()
		                                                      .findFirst();
		logger.debug(String.format("Fetched header information: %s", headerTitles.get()));
		return listToMap(headerTitles.get(), this.headerOffset.getStartColumnIndex());
	}

	/**
	 * Given the name of a column on the actual sheet, it returns its range in A1 notation,
	 * considering header definition.
	 *
	 * @param columnName the name of the column
	 * @return a range in A1 notation
	 */
	private String getRangeFromColumnName(String columnName) {
		final Map<String, Integer> header;
		try {
			header = this.getHeader();
			final Integer columnIndex = header.get(columnName);
			final GridRange columnRange = new GridRange().setStartColumnIndex(columnIndex)
			                                             .setStartRowIndex(headerOffset.getStartRowIndex() + 1)
			                                             .setEndColumnIndex(columnIndex);
			return A1NotationHelper.getNotationFromSheetNameAndGridRange(value.getProperties()
			                                                                  .getTitle(), columnRange);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Tries to find a particular row using a specified criteria.
	 *
	 * @param criteria key and value pairs defining columns and cells that needed to be found
	 * @return the row identifier if it exists, otherwise empty
	 */
	public Optional<Integer> getRowIdByColumnValues(Map<String, Object> criteria) throws IOException {
		final Map<String, Integer> header = this.getHeader();
		final List<String> ranges = criteria.keySet()
		                                    .stream()
		                                    .map(this::getRangeFromColumnName)
		                                    .collect(Collectors.toList());
		final BatchGetValuesResponse targetedColumns = repository.getMultipleRanges(spreadSheetId, ranges);
		List<String> targetedValues = mergeMultipleValueRanges(targetedColumns).concat("|")
		                                                                       .get();
		String searchValue = criteria.values()
		                             .stream()
		                             .map(Object::toString)
		                             .collect(Collectors.joining("|"));
		OptionalInt rowId = IntStream.range(headerOffset.getStartRowIndex() + 1, targetedValues.size())
		                             .filter(index -> targetedValues.get(index)
		                                                            .equals(searchValue))
		                             .findFirst();
		return rowId.isPresent() ?
		       Optional.of(rowId.getAsInt() + this.headerOffset.getStartRowIndex() + 2) :
		       Optional.empty();
	}

	/**
	 * Tries to find a particular row by using a specified criteria.
	 *
	 * @param criteria key and value pairs defining columns and cells that needed to be found
	 * @return the row itself if it exists, otherwise empty
	 */
	public Optional<List<Object>> getRowByColumnValues(Map<String, Object> criteria) throws IOException {
		Optional<Integer> rowId = this.getRowIdByColumnValues(criteria);
		if (!rowId.isPresent()) {
			return Optional.empty();
		}
		return this.getRowById(rowId.get());
	}

	/**
	 * Given a row identifier returns row data.
	 *
	 * @param rowId a row identifier
	 * @return {@code Optional} row values or empty() for none
	 */
	public Optional<List<Object>> getRowById(Integer rowId) throws IOException {
		// Should take into account not only rowId parameter but also header offset
		// Check if rowId position is after header
		GridRange rowRange = new GridRange().setStartRowIndex(rowId)
		                                    .setStartColumnIndex(this.headerOffset.getStartColumnIndex())
		                                    .setEndRowIndex(rowId)
		                                    .setEndColumnIndex(this.headerOffset.getEndColumnIndex());

		String range = getNotationFromSheetNameAndGridRange(value.getProperties()
		                                                         .getTitle(), rowRange);
		final ValueRange row = repository.getRange(spreadSheetId, range);
		return row.getValues()
		          .stream()
		          .findFirst();
	}

	/**
	 * Appends data at the end of a particular table.
	 *
	 * @param rowValues value to be added at the end of the table
	 * @return inserted values
	 */
	private CompletableFuture<ValueRange> appendRow(List<Object> rowValues) throws IOException {
		ValueRange appendRow = new ValueRange().setValues(Collections.singletonList(rowValues));
		final GridRange appendGridRange = new GridRange().setStartColumnIndex(headerOffset.getStartColumnIndex())
		                                                 .setStartRowIndex(headerOffset.getStartRowIndex());
		final String appendRange = getNotationFromSheetNameAndGridRange(this.getSheetTitle(), appendGridRange);
		final Sheets.Spreadsheets.Values.Append appendRequest = repository.append(spreadSheetId, appendRange, appendRow)
		                                                                  .setValueInputOption("USER_ENTERED")
		                                                                  .setInsertDataOption("INSERT_ROWS")
		                                                                  .setIncludeValuesInResponse(true);

		final CompletableFuture<ValueRange> appendedData = executor.getWithRetry(ctx -> appendRequest.execute()
		                                                                                             .getUpdates()
		                                                                                             .getUpdatedData());
		return appendedData;
	}

	/**
	 * Appends data after a particular table. Column information must be provided.
	 *
	 * @param row columns and their values to be appended at the end of the table
	 */
	public CompletableFuture<ValueRange> appendRow(Map<String, Object> row) throws IOException {
		// Get header information
		final Map<String, Integer> header = this.getHeader();
		// Create a new row with new values to be updated
		List<Object> newRowValues = IntStream.range(0, header.size())
		                                     .mapToObj(index -> getHeaderColumnNameByPosition(header, index))
		                                     .map(columnName -> getColumnValueForRow(row, columnName.get(), () -> ""))
		                                     .map(Object::toString)
		                                     .collect(Collectors.toList());
		return this.appendRow(newRowValues);
	}

	/**
	 * Appends or updates data on a particular table. Information about columns will be taken
	 * into consideration to update or create the row.
	 *
	 * @param row            columns and their and values to be updated or appended into the table
	 * @param keyColumns     columns that uniquely establish the identity of a row
	 * @param appendIfExists if the row already exists on the table it is updated otherwise appended
	 * @return
	 */
	private CompletableFuture<ValueRange> saveRow(Map<String, Object> row,
	                                              List<String> keyColumns,
	                                              boolean appendIfExists) throws IOException {
		// Prepare search criteria
		Map<String, Object> searchCriteria = new HashMap<>();
		keyColumns.stream()
		          .forEach(key -> searchCriteria.put(key, row.get(key)));

		// Search for the row in the current sheet
		Optional<Integer> searchForRowById = getRowIdByColumnValues(searchCriteria);

		// Get header information
		final Map<String, Integer> header = this.getHeader();

		// Create a new row with new values to be updated
		List<Object> newRowValues = IntStream.range(0, header.size())
		                                     .mapToObj(index -> getHeaderColumnNameByPosition(header, index))
		                                     .map(columnName -> getColumnValueForRow(row, columnName.get(), () -> ""))
		                                     .map(Object::toString)
		                                     .collect(Collectors.toList());

		// If row is present update the values
		if (searchForRowById.isPresent()) {
			ValueRange body = new ValueRange().setValues(Collections.singletonList(newRowValues));
			final GridRange updateGridRange = new GridRange().setStartRowIndex(searchForRowById.get())
			                                                 .setStartColumnIndex(headerOffset.getStartColumnIndex())
			                                                 .setEndRowIndex(searchForRowById.get());

			final String updateRange = getNotationFromSheetNameAndGridRange(getSheetTitle(), updateGridRange);
			final Sheets.Spreadsheets.Values.Update updateRequest = repository.update(spreadSheetId, updateRange, body)
			                                                                  .setIncludeValuesInResponse(true)
			                                                                  .setValueInputOption("USER_ENTERED");
			final CompletableFuture<ValueRange> updatedRow = executor.getWithRetry(ctx -> updateRequest.execute()
			                                                                                           .getUpdatedData());
			//updatedRow.thenAccept(updateValuesResponse -> logger.debug(updateValuesResponse.getValues().toString()));
			return updatedRow;
		}
		// Otherwise append the row at the end
		if (appendIfExists) {
			return appendRow(row);
		}

		return CompletableFuture.completedFuture(new ValueRange());
	}

	/**
	 * Appends or updates data on a particular table. Information about columns will be taken
	 * into consideration to update or create the row.
	 *
	 * @param row        columns and their and values to be updated or appended into the table
	 * @param keyColumns columns that uniquely establish the identity of a row
	 * @return
	 */
	public CompletableFuture<ValueRange> saveRow(Map<String, Object> row, List<String> keyColumns) throws IOException {
		return saveRow(row, keyColumns, true);
	}

	/**
	 * Updates data on a particular table. Information about columns will be taken
	 * into consideration to update the row.
	 *
	 * @param row        columns and their and values to be updated on the table
	 * @param keyColumns columns that uniquely establish the identity of a row
	 * @return
	 */
	public CompletableFuture<ValueRange> updateRow(Map<String, Object> row, List<String> keyColumns) throws IOException {
		return saveRow(row, keyColumns, false);
	}

	public GridRange getHeaderOffset() {
		return headerOffset;
	}

	/**
	 * Sets header location offset using a range on a sheet. All indexes are zero-based.
	 * Indexes are half open, e.g the start index is inclusive and the end index is exclusive
	 * -- [start_index, end_index).
	 * Missing indexes indicate the range is unbounded on that side.
	 *
	 * @param headerOffset A range on a sheet
	 * @return
	 */
	public GoogleSheet setHeaderOffset(GridRange headerOffset) {
		this.headerOffset = headerOffset;
		return this;
	}
}
