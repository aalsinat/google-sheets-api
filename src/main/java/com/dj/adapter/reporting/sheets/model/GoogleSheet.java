package com.dj.adapter.reporting.sheets.model;

import com.dj.adapter.reporting.sheets.service.GoogleSheetsService;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dj.adapter.reporting.sheets.utils.GoogleSheetsUtils.*;

public class GoogleSheet {
	/**
	 * Common instance for {@code empty()}.
	 */
	private static final GoogleSheet EMPTY = new GoogleSheet();
	private static final Integer DEFAULT_HEADER_ROW = 1;

	/**
	 * If non-null, the value; if null, indicates no value is present
	 */
	private final Sheet value;
	private GoogleSpreadsheet parent;
	private SheetProperties properties;
	private Integer headerRow;
	private ValueRange values;
	private GridRange headerOffset;
	private boolean refreshHeaderColumns;

	/**
	 * Constructs an empty instance.
	 */
	private GoogleSheet() {
		this.value = null;
		properties = null;
		headerOffset = null;
		refreshHeaderColumns = false;
		headerRow = DEFAULT_HEADER_ROW;
	}

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 * @throws NullPointerException if value is null
	 */
	private GoogleSheet(Sheet value, GoogleSpreadsheet parent) {
		this.value = Objects.requireNonNull(value);
		this.parent = Objects.requireNonNull(parent);
		properties = value.getProperties();
		headerOffset = new GridRange().setSheetId(value.getProperties()
		                                               .getSheetId());
		refreshHeaderColumns = false;
		headerRow = DEFAULT_HEADER_ROW;
	}

	/**
	 * Returns an empty {@code GoogleSheet} instance.  No value is present for this
	 * GoogleSheetsService.
	 *
	 * @return an empty {@code GoogleSheet}
	 * @apiNote Though it may be tempting to do so, avoid testing if an object
	 * is empty by comparing with {@code ==} against instances returned by
	 * {@code Option.empty()}. There is no guarantee that it is a singleton.
	 * Instead, use {@link #isPresent()}.
	 */
	public static GoogleSheet empty() {
		GoogleSheet t = EMPTY;
		return t;
	}

	/**
	 * Returns an {@code GoogleSheet} with the specified present non-null value.
	 *
	 * @param value the value to be present, which must be non-null
	 * @return a {@code GoogleSheet} with the value present
	 * @throws NullPointerException if value is null
	 */
	public static GoogleSheet of(Sheet value, GoogleSpreadsheet parent) {
		return new GoogleSheet(value, parent);
	}

	/**
	 * Returns an {@code GoogleSheet} describing the specified value, if non-null,
	 * otherwise returns an empty {@code GoogleSheet}.
	 *
	 * @param value the possibly-null value to describe
	 * @return an {@code GoogleSheet} with a present value if the specified value
	 * is non-null, otherwise an empty {@code GoogleSheet}
	 */
	static GoogleSheet ofNullable(Sheet value, GoogleSpreadsheet parent) {
		return value == null ? empty() : of(value, parent);
	}

	public void setRefreshHeaderColumns(boolean refreshHeaderColumns) {
		this.refreshHeaderColumns = refreshHeaderColumns;
	}

	/**
	 * Converts a list of objects to a map where the value is object's postion on the list
	 *
	 * @param items list of objects to be transformed
	 * @return a map for postions in the list
	 */
	private Map<String, Integer> listToMap(List<Object> items) {
		final Map<String, Integer> result = new HashMap<>();
		items.stream()
		     .forEach(item -> result.put(String.valueOf(item), items.indexOf(item)));
		return result;
	}

	private String getA1NotationFromGridRange(GridRange range) {
		return properties.getTitle() + "!" + Character.toString((char) (range.getStartColumnIndex() + 65)) +
		       (range.getStartRowIndex() + 1) + ":" + Character.toString((char) (range.getEndColumnIndex() + 65)) +
		       range.getEndRowIndex();
	}

	/**
	 * If a value is present in this {@code GoogleSheet}.
	 *
	 * @return the non-null value held by this {@code GoogleSheetsService}
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
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getSheetId() {
		return (isPresent()) ? Optional.of(properties.getSheetId()) : Optional.empty();
	}

	/**
	 * The number of rows in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getRowCount() {
		return (isPresent()) ? Optional.of(properties.getGridProperties()
		                                             .getRowCount()) : Optional.empty();
	}

	/**
	 * The number of columns in the grid.
	 *
	 * @return {@code Optional} value or empty() for none
	 */
	public Optional<Integer> getColumnCount() {
		return (isPresent()) ? Optional.of(properties.getGridProperties()
		                                             .getColumnCount()) : Optional.empty();
	}

	/**
	 * Sets header row.
	 *
	 * @param headerRow an integer setting row number for header
	 */
	public GoogleSheet setHeaderRow(Integer headerRow) {
		this.headerRow = headerRow;
		return this;
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

	/**
	 * Get current header values, based on the value of the range
	 * set for the header.
	 *
	 * @return
	 */
	public Map<String, Integer> getHeader() throws IOException {
		final List<Object> titles = readRow(headerRow).get();
		return listToMap(titles);
	}

	/**
	 * Tries to find a particular row using a specified criteria.
	 *
	 * @param criteria key and value pairs defining columns and cells that needed to be found
	 * @return
	 */
	public Optional<Integer> findRowIdByColumnValues(Map<String, Object> criteria) throws IOException {
		final Map<String, Integer> header = this.getHeader();
		final List<String> ranges = criteria.entrySet()
		                                    .stream()
		                                    .map(columnName -> header.get(columnName.getKey()))
		                                    .map(index -> String.format(
				                                    "%s%d:%s",
				                                    Character.toString((char) (index + 65)),
				                                    this.headerRow,
				                                    Character.toString((char) (index + 65))))
		                                    .collect(Collectors.toList());
		final BatchGetValuesResponse targetedColumns = GoogleSheetsService.getMultipleRanges(parent.getId(), ranges);
		List<String> targetedValues = mergeMultipleValueRanges(targetedColumns).concat("|")
		                                                                       .get();
		String searchValue = criteria.values()
		                             .stream()
		                             .map(Object::toString)
		                             .collect(Collectors.joining("|"));
		OptionalInt rowId = IntStream.range(headerRow, targetedValues.size())
		                             .filter(index -> targetedValues.get(index)
		                                                            .equals(searchValue))
		                             .findFirst();
		return rowId.isPresent() ? Optional.of(rowId.getAsInt() + headerRow + 1) : Optional.empty();
	}

	/**
	 * Given a row identifier returns row data.
	 *
	 * @param rowId a row identifier
	 * @return {@code Optional} row values or empty() for none
	 */
	public Optional<List<Object>> readRow(Integer rowId) throws IOException {
		String range = String.format("%s!%d:%d", properties.getTitle(), rowId, rowId);
		final ValueRange row = GoogleSheetsService.getRange(parent.getId(), range);
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
	private ValueRange appendRow(List<Object> rowValues) throws IOException {
		ValueRange appendRow = new ValueRange().setValues(Collections.singletonList(rowValues));
		AppendValuesResponse appendResult = GoogleSheetsService.append(parent.getId(), "A2", appendRow)
		                                                       .setValueInputOption("USER_ENTERED")
		                                                       .setInsertDataOption("INSERT_ROWS")
		                                                       .setIncludeValuesInResponse(true)
		                                                       .execute();
		return appendResult.getUpdates()
		                   .getUpdatedData();
	}

	/**
	 * Appends data after a particular table. Column information must be provided.
	 *
	 * @param row columns and their values to be appended at the end of the table
	 */
	public ValueRange appendRow(Map<String, Object> row) throws IOException {
		// Get header information
		final Map<String, Integer> header = this.getHeader();
		// Create a new row with new values to be updated
		List<Object> newRowValues = IntStream.range(0, header.size())
		                                     .mapToObj(index -> getHeaderColumnNameByPosition(header, index))
		                                     .map(columnName -> getColumnValueForRow(row, columnName.get(), () -> ""))
		                                     .map(Object::toString)
		                                     .collect(Collectors.toList());
		return appendRow(newRowValues);
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
	private ValueRange saveRow(Map<String, Object> row,
	                           List<String> keyColumns,
	                           boolean appendIfExists) throws IOException {
		// Prepare search criteria
		Map<String, Object> searchCriteria = new HashMap<>();
		keyColumns.stream()
		          .forEach(key -> searchCriteria.put(key, row.get(key)));

		// Search for the row in the current sheet
		Optional<Integer> searchForRowById = findRowIdByColumnValues(searchCriteria);

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
			UpdateValuesResponse updateResult =
					GoogleSheetsService.update(parent.getId(), String.format("A%d", searchForRowById.get()), body)
					                   .setIncludeValuesInResponse(true)
					                   .setValueInputOption("USER_ENTERED")
					                   .execute();
			return updateResult.getUpdatedData();
		}
		// Otherwise append the row at the end
		if (appendIfExists) {
			return appendRow(row);
		}

		return new ValueRange();
	}

	/**
	 * Appends or updates data on a particular table. Information about columns will be taken
	 * into consideration to update or create the row.
	 *
	 * @param row            columns and their and values to be updated or appended into the table
	 * @param keyColumns     columns that uniquely establish the identity of a row
	 * @return
	 */
	public ValueRange saveRow(Map<String, Object> row, List<String> keyColumns) throws IOException {
		return saveRow(row, keyColumns, true);
	}

	/**
	 * Updates data on a particular table. Information about columns will be taken
	 * into consideration to update the row.
	 *
	 * @param row            columns and their and values to be updated on the table
	 * @param keyColumns     columns that uniquely establish the identity of a row
	 * @return
	 */
	public ValueRange updateRow(Map<String, Object> row, List<String> keyColumns) throws IOException {
		return saveRow(row, keyColumns, false);
	}

}
