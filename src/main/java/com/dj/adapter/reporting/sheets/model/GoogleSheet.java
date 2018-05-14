package com.dj.adapter.reporting.sheets.model;

import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleSheet {
    /**
     * Common instance for {@code empty()}.
     */
    private static final GoogleSheet EMPTY = new GoogleSheet();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final Sheet value;
    private GoogleSpreadsheet parent;
    private SheetProperties properties;
    private Integer headerRow;
    private ValueRange values;
    private GridRange headerOffset;

    /**
     * Constructs an empty instance.
     */
    private GoogleSheet() {
        this.value = null;
        properties = null;
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     * @throws NullPointerException if value is null
     */
    private GoogleSheet(Sheet value) {
        this.value = Objects.requireNonNull(value);
        properties = value.getProperties();
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
     * @return an {@code GoogleSheet} with the value present
     * @throws NullPointerException if value is null
     */
    public static GoogleSheet of(Sheet value) {
        return new GoogleSheet(value);
    }

    /**
     * Returns an {@code GoogleSheet} describing the specified value, if non-null,
     * otherwise returns an empty {@code GoogleSheet}.
     *
     * @param value the possibly-null value to describe
     * @return an {@code GoogleSheet} with a present value if the specified value
     * is non-null, otherwise an empty {@code GoogleSheet}
     */
    static GoogleSheet ofNullable(Sheet value) {
        return value == null ? empty() : of(value);
    }

    private Map<Object, Integer> listToMap(List<Object> items) {
        final Map<Object, Integer> result = new HashMap<>();
        items.stream().forEach(item -> result.put(item, items.indexOf(item)));
        return result;
    }

    private String getA1NotationFromGridRange(GridRange range) {
        return properties.getTitle() + "!" +
                Character.toString((char) (range.getStartColumnIndex() + 65)) +
                (range.getStartRowIndex() + 1) +
                ":" +
                Character.toString((char) (range.getEndColumnIndex() + 65)) +
                range.getEndRowIndex();
    }

    public GoogleSheet setParent(GoogleSpreadsheet parent) {
        this.parent = parent;
        return this;
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
        return (isPresent()) ? Optional.of(properties.getGridProperties().getRowCount()) : Optional.empty();
    }

    /**
     * The number of columns in the grid.
     *
     * @return {@code Optional} value or empty() for none
     */
    public Optional<Integer> getColumnCount() {
        return (isPresent()) ? Optional.of(properties.getGridProperties().getColumnCount()) : Optional.empty();
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
     * @throws IOException
     */
    public Map<Object, Integer> getHeader() throws IOException {
        final List<Object> titles = readRow(headerRow).get();
        return listToMap(titles);
    }

    /**
     * @param criteria
     * @return
     * @throws IOException
     */
    public Optional<Integer> findRowIdByColumnValues(Map<String, Object> criteria) throws IOException {
        final Map<Object, Integer> header = this.getHeader();
        final List<String> ranges = criteria.entrySet().stream()
                                            .map(columnName -> header.get(columnName.getKey()))
                                            .map(index -> String.format("%s%d:%s", Character
                                                    .toString((char) (index + 65)), this.headerRow, Character
                                                                                .toString((char) (index + 65))))
                                            .collect(Collectors.toList());
        final BatchGetValuesResponse targetColumns = parent.getService().get().spreadsheets().values()
                                                           .batchGet(parent.getId()).setRanges(ranges).execute();
        System.out.println(targetColumns.values());
        return Optional.empty();
    }

    /**
     * Given a row identifier returns row data.
     *
     * @param rowId a row identifier
     * @return {@code Optional} row values or empty() for none
     */
    public Optional<List<Object>> readRow(Integer rowId) throws IOException {
        String range = String.format("%s!%d:%d", properties.getTitle(), rowId, rowId);
        final ValueRange row = parent.getService().get().spreadsheets().values()
                                     .get(parent.getId(), range)
                                     .execute();
        return row.getValues().stream().findFirst();
    }

    public void setValues(ValueRange values) {
        this.values = values;
    }

    /**
     * Appends data after a particular table.
     *
     * @param rowValues value to be added at the end of the table
     * @return inserted values
     */
    public ValueRange save(List<Object> rowValues) throws IOException {
        ValueRange appendRow = new ValueRange().setValues(Collections.singletonList(rowValues));
        AppendValuesResponse appendResult = parent.getService().get().spreadsheets().values()
                                                  .append(parent.getId(), "A2", appendRow)
                                                  .setValueInputOption("USER_ENTERED")
                                                  .setInsertDataOption("INSERT_ROWS")
                                                  .setIncludeValuesInResponse(true).execute();
        return appendResult.getUpdates().getUpdatedData();
    }


    /**
     * @param rowValues
     * @throws IOException
     */
    public void update(List<Object> rowValues) throws IOException {
        ValueRange updateRow = new ValueRange().setValues(Collections.singletonList(rowValues));
        UpdateValuesResponse updateResult = parent.getService().get().spreadsheets().values()
                                                  .update(parent.getId(), "A4", updateRow)
                                                  .setValueInputOption("RAW")
                                                  .execute();
    }

}