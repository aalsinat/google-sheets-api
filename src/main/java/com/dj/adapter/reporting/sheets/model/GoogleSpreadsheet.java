package com.dj.adapter.reporting.sheets.model;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.util.NoSuchElementException;
import java.util.Objects;

public class GoogleSpreadsheet {

	/**
	 * Common instance for {@code empty()}.
	 */
	private static final GoogleSpreadsheet EMPTY = new GoogleSpreadsheet();

	/**
	 * If non-null, the value; if null, indicates no value is present
	 */
	private final Spreadsheet value;

	/**
	 * Constructs an empty instance.
	 */
	private GoogleSpreadsheet() {
		this.value = null;
	}

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 * @throws NullPointerException if value is null
	 */
	private GoogleSpreadsheet(Spreadsheet value) {
		this.value = Objects.requireNonNull(value);
	}

	/**
	 * Returns an empty {@code GoogleSpreadsheet} instance.  No value is present for this
	 * GoogleSheetsService.
	 *
	 * @return an empty {@code GoogleSpreadsheet}
	 * @apiNote Though it may be tempting to do so, avoid testing if an object
	 * is empty by comparing with {@code ==} against instances returned by
	 * {@code Option.empty()}. There is no guarantee that it is a singleton.
	 * Instead, use {@link #isPresent()}.
	 */
	public static GoogleSpreadsheet empty() {
		GoogleSpreadsheet t = EMPTY;
		return t;
	}

	/**
	 * Returns an {@code GoogleSpreadsheet} with the specified present non-null value.
	 *
	 * @param value the value to be present, which must be non-null
	 * @return an {@code GoogleSpreadsheet} with the value present
	 * @throws NullPointerException if value is null
	 */
	public static GoogleSpreadsheet of(Spreadsheet value) {
		return new GoogleSpreadsheet(value);
	}

	/**
	 * Returns an {@code GoogleSpreadsheet} describing the specified value, if non-null,
	 * otherwise returns an empty {@code GoogleSpreadsheet}.
	 *
	 * @param value the possibly-null value to describe
	 * @return an {@code GoogleSpreadsheet} with a present value if the specified value
	 * is non-null, otherwise an empty {@code GoogleSpreadsheet}
	 */
	public static GoogleSpreadsheet ofNullable(Spreadsheet value) {
		return value == null ? empty() : of(value);
	}

	/**
	 * If a value is present in this {@code GoogleSpreadsheet}.
	 *
	 * @return the non-null value held by this {@code GoogleSheetsService}
	 * @throws NoSuchElementException if there is no value present
	 * @see GoogleSpreadsheet#isPresent()
	 */
	public Spreadsheet get() {
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
	 * Given a name, returns a {@link GoogleSheet} if it exists, otherwise empty.
	 *
	 * @param sheetName the name of the sheet to be retrieved
	 * @return a {@code GoogleSheet} or {@code empty()}
	 */
	public GoogleSheet getSheetByName(String sheetName) {
		final Sheet sheetToBeFound = value.getSheets()
		                                  .stream()
		                                  .filter(sheet -> sheet.getProperties()
		                                                        .getTitle()
		                                                        .equals(sheetName))
		                                  .findAny()
		                                  .get();
		return GoogleSheet.ofNullable(sheetToBeFound, this);
	}

	/**
	 * Returns the {@code GoogleSpreadsheet} identifier.
	 *
	 * @return the identifier of the spreadsheet
	 */
	public String getId() {
		return value.getSpreadsheetId();
	}
}
