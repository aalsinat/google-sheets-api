package com.dj.adapter.reporting.sheets.domain;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;
import java.util.Objects;

public class GoogleSpreadsheet {

	private final Spreadsheet value;
	private GoogleSheetsRepository repository;

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 * @throws NullPointerException if value is null
	 */
	public GoogleSpreadsheet(Spreadsheet value, GoogleSheetsRepository repository) {
		this.value = Objects.requireNonNull(value);
		this.repository = repository;
	}

	/**
	 * If a value is present in this {@code GoogleSpreadsheet}.
	 *
	 * @return the non-null value held by this {@code GoogleSheetsRepository}
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
		return new GoogleSheet(sheetToBeFound, repository).setSpreadSheetId(getId());
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
