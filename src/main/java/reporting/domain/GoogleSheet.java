package reporting.domain;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static reporting.domain.Range.RangeBuilder;
import static reporting.utils.A1NotationHelper.getNotationFromSheetNameAndGridRange;
import static reporting.utils.GoogleSheetsUtils.mergeMultipleValueRanges;

public class GoogleSheet {
	private static final Integer FIRST_ROW_INDEX = 0;

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(GoogleSheet.class);
	private final Sheet value;
	private final GoogleSheetsRepository repository;
	private Header header;
	private String spreadSheetId;

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param spreadSheetId
	 * @param value         the non-null value to be present
	 */
	@Autowired
	public GoogleSheet(String spreadSheetId, Sheet value, GoogleSheetsRepository repository) throws IOException {
		logger.debug("A new instance of GoogleSheet with id {} and title '{}' is being created. ",
		             value
				             .getProperties()
				             .getSheetId(),
		             value
				             .getProperties()
				             .getTitle());
		this.value = Objects.requireNonNull(value);
		this.repository = repository;
		this.spreadSheetId = spreadSheetId;
		setHeader(FIRST_ROW_INDEX);
	}

	/**
	 * Retuns parent spreadsheet identifier.
	 *
	 * @return identifier of the spreadsheet where it belongs
	 */
	public String getSpreadSheetId() {
		return spreadSheetId;
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
		if (this.isPresent()) {
			return value
					.getProperties()
					.getSheetId();
		}
		throw new NullPointerException("getSheetId: Google sheet is empty, so there is no identifier to be returned.");
	}

	/**
	 * The title of the sheet.
	 *
	 * @return value for the title of actual sheet
	 */
	public String getSheetTitle() {
		if (this.isPresent()) {
			return value
					.getProperties()
					.getTitle();
		}
		throw new NullPointerException("getSheetTitle: Google sheet is empty, so there is no title to be returned.");
	}

	/**
	 * Sets range values for current sheet header. Both starting row index and starting column index are zero-based values.
	 *
	 * @param rowIndex    row index where the header is located
	 * @param columnIndex starting column index where the header is located
	 * @return updated sheet with new header values
	 * @throws IOException
	 */
	public GoogleSheet setHeader(Integer rowIndex, Integer columnIndex) throws IOException {
		header = new Header(rowIndex, columnIndex);
		fetchHeaderValues();
		return this;
	}

	/**
	 * Support method for retrieving header values according to defined header range values.
	 *
	 * @throws IOException
	 */
	private void fetchHeaderValues() throws IOException {
		final String headerRange = header.getRangeInA1Notation(this.getSheetTitle());
		final Optional<List<Object>> headerTitles;
		try {
			headerTitles = repository
					.getRangeWithRetry(spreadSheetId, headerRange)
					.thenApply(response -> response
							.getValues()
							.stream()
							.findFirst())
					.get();
			logger.debug(String.format("Fetched header information: %s", headerTitles.get()));
			headerTitles.ifPresent(titles -> header.setColumns(titles));
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(String.format("Error fetching header values: %s", e.getLocalizedMessage()));
		} catch (ExecutionException e) {
			logger.error(String.format("Error fetching header values: %s", e.getLocalizedMessage()));
		}
	}

	/**
	 * Returns number of rows in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getRowCount() {
		return (isPresent()) ?
		       Optional.of(value
				                   .getProperties()
				                   .getGridProperties()
				                   .getRowCount()) :
		       Optional.empty();
	}

	public CompletableFuture<Integer> getFilledRowsCount(List<String> keyColumns) throws IOException {

		final List<String> ranges = keyColumns.stream().map(column -> header
				.getColumnRangeInA1Notation(column, this.getSheetTitle()))
		                                      .filter(Objects::nonNull)
		                                      .collect(Collectors.toList());

		final CompletableFuture<BatchGetValuesResponse> targetedColumnsFuture = repository
				.getMultipleRangesWithRetry(spreadSheetId, ranges);

		final CompletableFuture<Integer> rowCountResponse = targetedColumnsFuture.thenApply(response -> {
			// If there are no results after the request
			if (isTargetedColumnsEmpty(response)) return 0;

			List<String> targetedValues = mergeMultipleValueRanges(response)
					.concat("|")
					.get();
			logger.info("Targeted values: {}", targetedValues.size());
			return targetedValues.size();
		});
		return rowCountResponse;
	}

	/**
	 * Returns number of columns in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getColumnCount() {
		return (isPresent()) ?
		       Optional.of(value
				                   .getProperties()
				                   .getGridProperties()
				                   .getColumnCount()) :
		       Optional.empty();
	}

	/**
	 * Return the header of current sheet instance.
	 *
	 * @return an instance of {@code Header} that represents the header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Sets range values for current sheet header. Starting row index is zero-based.
	 *
	 * @param rowIndex row index where the header is located
	 * @return updated sheet with new header values
	 */
	public GoogleSheet setHeader(Integer rowIndex) throws IOException {
		header = new Header(rowIndex);
		fetchHeaderValues();
		return this;
	}

	/**
	 * Get current header values and their positions on the sheet.
	 *
	 * @return
	 */
	public Map<String, Integer> getHeaderValues() throws IOException {
		return header.toMap();
	}

	/**
	 * Tries to find a particular row using a specified criteria.
	 *
	 * @param criteria key and value pairs defining columns and cells that needed to be found
	 * @return the row identifier if it exists, otherwise empty
	 */
	public CompletableFuture<Optional<Integer>> findRowIdByColumnValues(Map<String, Object> criteria) throws IOException {
		// TODO: Create a new entity for search criteria or query conditions
		final List<String> ranges = criteria
				.keySet()
				.stream()
				.map(column -> header.getColumnRangeInA1Notation(column, this.getSheetTitle()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		String searchValue = criteria
				.values()
				.stream()
				.map(Object::toString)
				.collect(Collectors.joining("|"));

		final CompletableFuture<BatchGetValuesResponse> targetedColumnsFuture = repository.getMultipleRangesWithRetry(
				spreadSheetId,
				ranges);

		final CompletableFuture<Optional<Integer>> findRowIdResponse = targetedColumnsFuture.thenApply(response -> {

			// If there are no results after the request
			if (isTargetedColumnsEmpty(response)) return Optional.empty();

			List<String> targetedValues = mergeMultipleValueRanges(response)
					.concat("|")
					.get();

			// TODO: Try to do a reverse loop on searching for the row, starting from targetedValues.size() through start row index
			OptionalInt rowId = IntStream
					.range(0, targetedValues.size())
					.filter(index -> targetedValues
							.get(index)
							.equals(searchValue))
					.findFirst();
			return rowId.isPresent() ?
			       Optional.of(rowId.getAsInt() + header
					       .getRange()
					       .getStartRowIndex() + 1) :
			       Optional.empty();
		});

		return findRowIdResponse;
	}

	/**
	 * Tries to find a particular row by using a specified criteria.
	 *
	 * @param criteria key and value pairs defining columns and cells that needed to be found
	 * @return the row itself if it exists, otherwise empty
	 */
	public Optional<List<Object>> findRowByColumnValues(
			Map<String, Object> criteria) throws IOException, ExecutionException, InterruptedException {
		Optional<Integer> rowId = this
				.findRowIdByColumnValues(criteria)
				.get();
		if (!rowId.isPresent()) {
			return Optional.empty();
		}
		return this.getRowById(rowId.get());
	}

	private boolean isTargetedColumnsEmpty(BatchGetValuesResponse targetedColumns) {
		if (targetedColumns
				.getValueRanges()
				.stream()
				.map(ValueRange::getValues)
				.filter(Objects::nonNull)
				.count() == 0)
			return true;
		return false;
	}

	/**
	 * Given a row identifier returns row data.
	 *
	 * @param rowId a row identifier
	 * @return {@code Optional} row values or empty() for none
	 */
	public Optional<List<Object>> getRowById(Integer rowId) throws IOException, ExecutionException, InterruptedException {
		// TODO: Check if rowId position is after header
		GridRange rowRange = new GridRange().setStartRowIndex(rowId)
		                                    .setStartColumnIndex(header.getRange()
		                                                               .getStartColumnIndex())
		                                    .setEndRowIndex(rowId)
		                                    .setEndColumnIndex(header.getRange()
		                                                             .getEndColumnIndex());

		String range = getNotationFromSheetNameAndGridRange(value.getProperties()
		                                                         .getTitle(), rowRange);
		final CompletableFuture<ValueRange> row = repository.getRangeWithRetry(spreadSheetId, range);
		return row.get()
		          .getValues()
		          .stream()
		          .findFirst();
	}

	/**
	 * Appends data at the end of a particular table.
	 *
	 * @param rowValues value to be added at the end of the table
	 * @return response object from google api
	 */
	private ValueRange appendRow(List<Object> rowValues) throws IOException {
		ValueRange appendRow = new ValueRange().setValues(Collections.singletonList(rowValues));
		final String appendRange = header.getStartRangeInA1Notation(getSheetTitle());
		return repository
				.append(spreadSheetId, appendRange, appendRow)
				.getUpdates()
				.getUpdatedData();
	}

	/**
	 * Asynchronous and retry version of {@code append}.
	 *
	 * @param rowValues value to be added at the end of the table
	 * @return a {@link CompletableFuture} with inserted values
	 */
	private CompletableFuture<ValueRange> appendRowWithRetry(List<Object> rowValues) throws IOException {
		ValueRange appendRow = new ValueRange().setValues(Collections.singletonList(rowValues));
		final String appendRange = header.getStartRangeInA1Notation(getSheetTitle());
		return repository
				.appendWithRetry(spreadSheetId, appendRange, appendRow)
				.thenApply(response -> response
						.getUpdates()
						.getUpdatedData());
	}

	/**
	 * Appends data after a particular table. Column information must be provided.
	 *
	 * @param row columns and their values to be appended at the end of the table
	 */
	public CompletableFuture<ValueRange> appendRow(Row row) throws IOException {
		// Create a new row with new values to be updated
		List<Object> newRowValues = row.merge(header);
		logger.debug("Async request to append row with row values {}", newRowValues);
		return this
				.appendRowWithRetry(newRowValues)
				.thenApply(Function.identity());
	}

	/**
	 * Appends a list of rows after a particular table. Column information must be provided.
	 *
	 * @param rows a list of rows, each one containing columns and their values
	 */
	public CompletableFuture<List<ValueRange>> appendRows(
			List<Row> rows) throws IOException, ExecutionException, InterruptedException {
		final Sheets.Spreadsheets.Values.BatchUpdate batchUpdate;

		final Integer rowsCount = this.getFilledRowsCount(Arrays.asList("Id")).get();

		final BatchUpdateSpreadsheetResponse appendDimension = repository
				.appendDimension(spreadSheetId, this.getSheetId(), rows.size())
				.get();

		final List<ValueRange> newRows = IntStream
				.range(0, rows.size())
				.mapToObj(rowIndex -> getLocatedRow(rows.get(rowIndex),
				                                    rowIndex + rowsCount))
				.collect(Collectors.toList());

		batchUpdate = repository.batchUpdate(spreadSheetId,
		                                     new BatchUpdateValuesRequest()
				                                     .setData(newRows)
				                                     .setValueInputOption("USER_ENTERED")
				                                     .setResponseDateTimeRenderOption(
						                                     "FORMATTED_STRING")
				                                     .setIncludeValuesInResponse(true));
		return repository
				.getExecutor()
				.getWithRetry(ctx -> batchUpdate
						.execute()
						.getResponses()
						.stream()
						.map(response -> response.getUpdatedData())
						.collect(Collectors.toList()));
	}

	/**
	 * Support method for creating {@link ValueRange}s based on rows and a relative position from header.
	 *
	 * @param row      {@code Row} containing all data to be added to {@code ValueRange}
	 * @param position Relative position from header
	 * @return a new {@code ValueRange} containing provided values and range
	 */
	private ValueRange getLocatedRow(Row row, Integer position) {
		GridRange headerRange = header.getRange();
		GridRange rowRange = new GridRange()
				.setStartRowIndex(headerRange.getStartRowIndex() + 1 + position)
				.setStartColumnIndex(headerRange.getStartColumnIndex());

		String range = new RangeBuilder(this.getSheetTitle(),
		                                header

				                                .getRange()
				                                .getStartRowIndex() + 1 + position)
				.withStartingColumn(header
						                    .getRange()
						                    .getStartColumnIndex())
				.build()
				.getRangeInA1Notation();
		//Range range = new Range(rowRange, this.getSheetTitle());
		return new ValueRange()
				.setValues(Collections.singletonList(row.merge(header)))
				.setRange(range);
	}

	/**
	 * Appends or updates data on a particular table. Information about columns will be taken
	 * into consideration to update or create the row.
	 *
	 * @param row               columns and their and values to be updated or appended into the table
	 * @param keyColumns        columns that uniquely establish the identity of a row
	 * @param appendIfNotExists if the row already exists on the table it is updated otherwise appended
	 * @return
	 */
	private CompletableFuture<ValueRange> saveRow(Row row, List<String> keyColumns,
	                                              boolean appendIfNotExists) throws IOException {
		// Prepare search criteria
		Map<String, Object> searchCriteria = new HashMap<>();
		keyColumns.stream()
		          .forEach(key -> searchCriteria.put(key,
		                                             row.findCellByColumnName(key)
		                                                .getValue()));

		// Search for the row in the current sheet
		try {
			//TODO: Create a method for retrieving both, row identifier and its values, to avoid to requests to API.
			// Blocking call for search row identifier using search criteria
			final Optional<Integer> rowId = findRowIdByColumnValues(searchCriteria).get();
			if (!rowId.isPresent() && appendIfNotExists) {
				return appendRow(row);
			}

			final GridRange updateGridRange = new GridRange().setStartRowIndex(rowId.get())
			                                                 .setStartColumnIndex(header.getRange()
			                                                                            .getStartColumnIndex())
			                                                 .setEndRowIndex(rowId.get());
			final String updateRange = getNotationFromSheetNameAndGridRange(getSheetTitle(), updateGridRange);
			// Blocking call for searching existing row values
			List<List<Object>> existingRow = repository.getRangeWithRetry(this.spreadSheetId, updateRange)
			                                           .get()
			                                           .getValues();

			ValueRange body = new ValueRange().setValues(Collections.singletonList(row.merge(header, existingRow)));
			// Asynchronous and with retries request for update a row
			final CompletableFuture<ValueRange> updatedRow = repository.updateWithRetry(spreadSheetId, updateRange, body)
			                                                           .thenApply(response -> response.getUpdatedData());
			return updatedRow;
		} catch (InterruptedException e) {
			logger.error("Asynchronous and with retries execution of save row has failed: {}", e.getLocalizedMessage());
		} catch (ExecutionException e) {
			logger.error("Asynchronous and with retries execution of save row has failed: {}", e.getLocalizedMessage());
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
	public CompletableFuture<ValueRange> saveRow(Row row, List<String> keyColumns) throws IOException {
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
	public CompletableFuture<ValueRange> updateRow(Row row, List<String> keyColumns) throws IOException {
		return saveRow(row, keyColumns, false);
	}

}
