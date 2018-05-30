package com.dj.adapter.reporting.sheets;

import com.dj.adapter.reporting.sheets.domain.*;
import com.dj.adapter.reporting.sheets.service.SheetsReportingService;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SheetsApplicationTests {
	final String spreadsheetId = "1A-C_yPt34w3fX2ZUxveH2qpPoiCVDQ7L7a9MdwXvCBY";
	@Autowired
	SheetsReportingService service;
	@Autowired
	private GoogleSheetsRepositoryFactory repositoryFactory;
	@Autowired
	private GoogleSheetsRepository repository;
	/*
	 * Mocking data structures
	 */
	private Map<String, Object> header = new HashMap<>();
	private Map<String, Object> searchCriteria = new HashMap<>();
	private Map<String, Object> validRow = new HashMap<>();
	private Map<String, Object> invalidRow = new HashMap<>();
	private Map<String, Object> rowToBeAppended = new HashMap<>();
	private Map<String, Object> rowToBeUpdated = new HashMap<>();

	private GoogleSpreadsheet spreadsheet;
	private GoogleSheet sheet;

	/**
	 * Method for formatting Date variables coming from task request
	 */
	private static Object dateToStringFormat(String field, Object value) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return df.format((Date) value);
		} else {
			return value.toString();
		}
	}

	/**
	 * Method for formatting meta variables coming from task request
	 */
	private static Object metaVariablesFormat(String field, Object value) {
		if ("_current_date".equals(field)) {
			return Calendar.getInstance().getTime();
		} else {
			return value;
		}
	}

	private static Object checkForNullValues(String field, Object value) {
		return value == null ? "" : value;
	}

	/**
	 * Method that check the maximum length allowed for a cell
	 */
	public static Object checkMaximumLength(String field, Object value) {
		if ((value instanceof String) && ((String) value).length() > 50000) {
			return ((String) value).substring(0, 49999);
		} else {
			return value;
		}
	}


	@Before
	public void setUp() throws IOException {
		this.spreadsheet = service.getSpreadSheetById(spreadsheetId);
		this.sheet = service.getSheetByName(spreadsheetId, "Class Data");
		this.sheet.setHeaderOffset(new GridRange().setStartRowIndex(1).setStartColumnIndex(0).setEndRowIndex(1));

		// Header for testing definition
		header.put("Id", 0);
		header.put("Student Name", 1);
		header.put("Gender", 2);
		header.put("Class Level", 3);
		header.put("Home State", 4);
		header.put("Major", 5);
		header.put("Extracurricular Activity", 6);

		// Create a search criteria
		searchCriteria.put("Student Name", "Benjamin");
		searchCriteria.put("Gender", "Male");
		searchCriteria.put("Home State", "BCN");

		// Create a valid test row
		validRow.put("Student Name", "Benjamin");
		validRow.put("Gender", "Female");
		validRow.put("Home State", "BDN");

		// Create an invalid test row
		invalidRow.put("Gender", "Male");
		invalidRow.put("Bad column name", "Whatever");

		// Create a row to be appended
		rowToBeAppended.put("Id", 0);
		rowToBeAppended.put("Student Name", "Alex");
		rowToBeAppended.put("Gender", "Male");
		rowToBeAppended.put("Class Level", "4. Senior");
		rowToBeAppended.put("Home State", "BCN");
		rowToBeAppended.put("Major", "Catalan");
		rowToBeAppended.put("Extracurricular Activity", "Basketball");

		// Create a row to be updated
		rowToBeUpdated.put("Id", 0);
		rowToBeUpdated.put("Student Name", "Carrie");
		rowToBeUpdated.put("Gender", "Female");
		rowToBeUpdated.put("Class Level", "3. Junior");
		rowToBeUpdated.put("Home State", "NE");
		rowToBeUpdated.put("Major", "English");
		rowToBeUpdated.put("Extracurricular Activity", "Track & Field");
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void sheetsServicesIsCreatedAndInjected() {
		assertNotNull(repositoryFactory);
		assertNotNull("A new Sheets service has been created", repository);
		assertTrue("Service has right application name", repository.get().getApplicationName() == "FlowIT Reporting");
	}

	@Test
	public void mockingEnvironmentIsCreated() {
		assertTrue("A new spreadsheet is instantiated", this.spreadsheet.isPresent());
		assertTrue("A sheet with 'Class Data' name was found", this.sheet.isPresent());
		assertTrue("A search criteria has been created", this.searchCriteria != null && !this.searchCriteria.isEmpty());
		System.out.println();
	}

	/**
	 * Get and set header properties for {@link GoogleSheet}
	 */
	@Test
	public void googleSheetSetHeaderRowAndGetData() throws IOException {
		final Map<String, Integer> header = this.sheet.getHeader();
		assertTrue("Header exists,", header != null && !header.isEmpty());
		assertTrue("Number of columns for header is 7", header.size() == 7);
		System.out.println(String.format("Header is at row number 2, and its data is: %s", header.toString()));
	}

	/**
	 * Check Id property for {@link GoogleSheet}
	 */
	@Test
	public void googleSheetCheckIdProperty() {
		final Integer sheetId = this.sheet.getSheetId();
		assertNotNull("Found sheet has an Id", sheetId);
		assertTrue("Sheet identifier must be non-negative", sheetId >= 0);
	}

	/**
	 * Check RowCount property for {@link GoogleSheet}
	 */
	@Test
	public void googleSheetCheckRowCountProperty() {

		final Optional<Integer> rowCount = this.sheet.getRowCount();
		assertTrue("The number of rows in the grid is non-negative", rowCount.get() > 0);
		System.out.println(String.format("Number of rows in the grid: %d", rowCount.get()));
	}

	/**
	 * Check ColumnCount propertyf or {@link GoogleSheet}
	 */
	@Test
	public void googleSheetCheckColumnCountProperty() {
		final Optional<Integer> columnCount = this.sheet.getColumnCount();
		assertTrue("The number of columns in the grid is non-negative", columnCount.get() > 0);
		System.out.println(String.format("Number of columns in the grid: %d", columnCount.get()));
	}

	/**
	 * Check append operation for {@link GoogleSheet}
	 */
	@Test
	public void googleSheetCheckAppendOperation() throws IOException {
		final CompletableFuture<ValueRange> appended = this.sheet.appendRow(this.rowToBeAppended);
		appended.thenAccept(valueRange -> {
			try {
				System.out.println(String.format("Inserted values: %s", valueRange.toPrettyString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Testing find row by multiple columns
	 */
	@Test
	public void searchARowByMultipleColumnsValues() throws IOException {
		final Map<String, Object> searchCriteria = new HashMap<>();
		searchCriteria.put("Student Name", "Carrie");
		searchCriteria.put("Gender", "Female");
		final Optional<Integer> rowIdByColumnValues = this.sheet.getRowIdByColumnValues(searchCriteria);
		assertTrue("Searched criteria is present.", rowIdByColumnValues.isPresent());
		System.out.println(String.format("Search value is at row number %d", rowIdByColumnValues.get()));
	}

	/**
	 * Check that save operation updates a row if already exists, otherwise append the row at the end
	 */
	@Test
	public void whenRowExistsThenSaveActuallyUpdatesValues() throws IOException {
		final List<String> keyColumns = Arrays.asList("Student Name", "Gender", "Major");
		final Integer oldIdValue = Integer.valueOf(this.rowToBeUpdated.get("Id").toString()) + 1000;
		this.rowToBeUpdated.put("Id", oldIdValue);
		CompletableFuture<ValueRange> saved = this.sheet.saveRow(this.rowToBeUpdated, keyColumns);
		saved.thenAccept(value -> System.out.println(value.getValues().toString())).thenRun(() -> {
			System.out.println("Completable Future completed");
			assertEquals("Save actually updates an existing row", 1, 0);

		});

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void checkValidationAndFormattingPolicies() {
		final FormattingPolicy formattingPolicy = new FormattingPolicy()
				.applyFormat(SheetsApplicationTests::metaVariablesFormat, SheetsApplicationTests::dateToStringFormat);

		ValidationPolicy validationPolicy = new ValidationPolicy()
				.applyValidation(SheetsApplicationTests::checkForNullValues, SheetsApplicationTests::checkMaximumLength);

		CellBuilder cellBuilder = new CellBuilder(formattingPolicy, validationPolicy);

		Cell cellOne = cellBuilder.create("Test", Calendar.getInstance().getTime());

		Cell cellTwo = cellBuilder.create("_current_date", "Whatever");

		assertTrue(cellOne.getValue().getClass().getName().equals("java.lang.String"));
	}

}
