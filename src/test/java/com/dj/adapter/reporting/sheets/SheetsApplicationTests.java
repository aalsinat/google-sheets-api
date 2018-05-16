package com.dj.adapter.reporting.sheets;

import com.dj.adapter.reporting.sheets.configuration.ReportingConfiguration;
import com.dj.adapter.reporting.sheets.model.GoogleSheet;
import com.dj.adapter.reporting.sheets.model.GoogleSpreadsheet;
import com.dj.adapter.reporting.sheets.service.GoogleSheetsService;
import com.dj.adapter.reporting.sheets.service.ServiceFactory;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dj.adapter.reporting.sheets.utils.GoogleSheetsUtils.getColumnValueForRow;
import static com.dj.adapter.reporting.sheets.utils.GoogleSheetsUtils.getHeaderColumnNameByPosition;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//@RunWith(SpringRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ReportingConfiguration.class})
//@SpringBootTest
public class SheetsApplicationTests {
	final String spreadsheetId = "1A-C_yPt34w3fX2ZUxveH2qpPoiCVDQ7L7a9MdwXvCBY";

	@Autowired
	private ServiceFactory serviceFactory;

	@Autowired
	private GoogleSheetsService sheetsService;

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

	@Before
	public void setUp() throws IOException {

		this.spreadsheet = sheetsService.getSpreadSheetById(spreadsheetId);
		this.sheet = spreadsheet.getSheetByName("Class Data");
		this.sheet.setHeaderRow(2);

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
		assertNotNull(serviceFactory);
		assertNotNull("A new Sheets service has been created", sheetsService);
		assertTrue("Service has right application name",
		           sheetsService.get()
		                        .getApplicationName() == "FlowIT Reporting");
	}

	@Test
	public void mockingEnvironmentIsCreated() {
		assertTrue("A new spreadsheet is instantiated", this.spreadsheet.isPresent());
		assertTrue("A sheet with 'Class Data' name was found", this.sheet.isPresent());
		assertTrue("A search criteria has been created", this.searchCriteria != null && !this.searchCriteria.isEmpty());
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
		final Optional<Integer> sheetId = this.sheet.getSheetId();
		assertTrue("Found sheet has an Id", sheetId.isPresent());
		assertTrue("Sheet identifier must be non-negative", sheetId.get() >= 0);
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
		Optional<Integer> rowCount = this.sheet.getRowCount();
		final ValueRange appended = this.sheet.appendRow(this.rowToBeAppended);
		System.out.println(String.format("Inserted values: %s", appended.toPrettyString()));
	}

	/**
	 * Testing find row by multiple columns
	 */
	@Test
	public void searchARowByMultipleColumnsValues() throws IOException {
		final Map<String, Object> searchCriteria = new HashMap<>();
		searchCriteria.put("Student Name", "Carrie");
		searchCriteria.put("Gender", "Female");
		final Optional<Integer> rowIdByColumnValues = this.sheet.findRowIdByColumnValues(searchCriteria);
		assertTrue("Searched criteria is present.", rowIdByColumnValues.isPresent());
		System.out.println(String.format("Search value is at row number %d", rowIdByColumnValues.get()));
	}

	/**
	 * Check that save operation updates a row if already exists, otherwise append the row at the end
	 */
	@Test
	public void whenRowExistsThenSaveActuallyUpdatesValues() throws IOException {
		final List<String> keyColumns = Arrays.asList("Student Name", "Gender", "Major");
		Integer oldIdValue = Integer.valueOf(this.rowToBeUpdated.get("Id")
		                                                        .toString());
		this.rowToBeUpdated.put("Id", ++oldIdValue);
		final ValueRange saved = this.sheet.saveRow(this.rowToBeUpdated, keyColumns);

		// Create search criteria
		Map<String, Object> searchCriteria = new HashMap<>();
		keyColumns.stream()
		          .forEach(key -> searchCriteria.put(key, this.rowToBeUpdated.get(key)));

		final Optional<Integer> searchedRowId = this.sheet.findRowIdByColumnValues(searchCriteria);
		final Optional<List<Object>> updatedRow = this.sheet.readRow(searchedRowId.get());

		assertTrue("Save actually updates an existing row",
		           oldIdValue == Integer.valueOf(updatedRow.get()
		                                                   .get(0)
		                                                   .toString()));
	}

}
