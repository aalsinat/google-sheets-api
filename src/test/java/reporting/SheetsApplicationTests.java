package reporting;

import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.http.client.config.RequestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reporting.domain.*;
import reporting.retry.AsyncRetryExecutor;
import reporting.service.SheetsReportingService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SheetsApplicationTests {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(SheetsApplicationTests.class);
	final String BENCHMARK_ONE = "1IRpvVVmEt9uKOjkEfabZf2KgE2LIQZ1y-prempVTzkg";
	final String BENCHMARK_TWO = "1fpDiSdTbq03udvYj1j3Y4DiYKszpAPe03IqgWejgZwY";
	final String AME_COPY_PROD = "1J8fL48DX6bXTO8FPtb032genJvGQf455fI7lqNDBnJc";

	final String SHEET_ONE = "Benchmark1";
	final String SHEET_TWO = "Benchmark2";
	final String SHEET_AME = "Union";

	@Autowired
	SheetsReportingService service;
	@Autowired
	private GoogleSheetsRepositoryFactory repositoryFactory;
	@Autowired
	private GoogleSheetsRepository repository;
	@Autowired
	private RowTemplate rowTemplate;
	@Autowired
	private AsyncRetryExecutor executor;
	/*
	 * Mocking data structures
	 */
	private Map<String, String> columnDefinition = new HashMap<>();
	private DateFormat df;
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
		if ((value instanceof String) && ((String) value).length() > 10) {
			return ((String) value).substring(0, 9);
		} else {
			return value;
		}
	}


	@Before
	public void setUp() throws IOException {
		// Header column definition
		columnDefinition.put("Id", "Id");
		columnDefinition.put("Name", "Name");
		columnDefinition.put("Date", "Date");

		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	}

	/**
	 * Creates a mocking row based on an index number.
	 *
	 * @param index
	 * @return
	 */
	private Row mockingRow(Integer index) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Map<String, Object> cells = new HashMap<>();
		cells.put("Id", index);
		cells.put("Name", String.format("Name for cell %d", index));
		cells.put("Date", df.format(Calendar.getInstance().getTime()));
		return rowTemplate.build().addCells(cells);
	}

	private Row mockingAmeRow(Integer index) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

		Map<String, Object> cells = new HashMap<>();
		cells.put("Origin", "AME-Test Units");
		cells.put("Editor", "FlowIT");
		cells.put("Language", "Catalan");
		cells.put("Country", "Spain");
		cells.put("Task Creation Date/Time", df.format(Calendar.getInstance().getTime()));
		cells.put("Status", "testing append");
		cells.put("Article count", index);
		cells.put("Completed Date", df.format(Calendar.getInstance().getTime()));
		cells.put("Accession Numbers", "PATHAR0020180703ee7300002,PATHAR0020180704ee7300001,PATHAR0020180624ee6o0000a");
		cells.put("Article count(ES)", index);
		cells.put("Accession Numbers(ES)", "PATHAR0020180703ee7300002,PATHAR0020180704ee7300001,PATHAR0020180624ee6o0000a");
		cells.put("Article count(Class)", 0);
		cells.put("Accession Numbers(Class)", "");
		cells.put("Completed Article Count", index);
		cells.put("Incomplete Article Count", 0);
		cells.put("Timely Article Count", index);
		cells.put("Untimely Article Count", 0);
		cells.put("Confidence", "");
		return rowTemplate.build().addCells(cells);
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void appendingSynchronouslyInsertsInRightOrder() throws IOException {
		GoogleSheet sheetOne = service.getSheetByName(BENCHMARK_ONE, SHEET_ONE);
		GoogleSheet sheetTwo = service.getSheetByName(BENCHMARK_TWO, SHEET_TWO);
		IntStream.range(1, 300).forEach(index -> {
			Row row = this.mockingRow(index);
			try {
				sheetOne.appendRow(row)
				        .thenAccept(valueRange -> logger.info("Appended a new row to Sheet1 -> {}", valueRange.getValues()))
				        .get();
				sheetTwo.appendRow(row)
				        .thenAccept(valueRange -> logger.info("Appended a new row to Sheet2 -> {}", valueRange.getValues()))
				        .get();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void appendingAsynchronouslyInsertsInRightOrder() throws IOException, ExecutionException, InterruptedException {
		GoogleSheet sheetOne = service.getSheetByName(BENCHMARK_ONE, SHEET_ONE);
		GoogleSheet sheetTwo = service.getSheetByName(BENCHMARK_TWO, SHEET_TWO);

		final String s = executor.getWithRetry(ctx -> {
			IntStream.range(1, 300).forEach(index -> {
				Row row = this.mockingRow(index);
				try {
					sheetOne.appendRow(row)
					        .thenAccept(valueRange -> logger.info("Appended a new row to Sheet1 -> {}", valueRange.getValues()));
					sheetTwo.appendRow(row)
					        .thenAccept(valueRange -> logger.info("Appended a new row to Sheet2 -> {}", valueRange.getValues()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			return "Asynchronous request finished";
		}).get();
		logger.info(s);
		Thread.sleep(600000);
	}


	@Test
	public void appendingSynchronouslyIntoAMEInsertsInRightOrder() throws IOException, ExecutionException, InterruptedException {
		GoogleSheet sheet = service.getSheetByName(AME_COPY_PROD, SHEET_AME).setHeader(4);
		final String s = executor.getWithRetry(ctx -> {
			IntStream.range(1, 300).forEach(index -> {
				Row row = this.mockingAmeRow(index);
				try {
					sheet.appendRow(row)
					     .thenAccept(valueRange -> logger.info("Appended a new row to AME-> {}", valueRange.getValues())).get();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			});
			return "Asynchronous request finished";
		}).get();
		logger.info(s);
		Thread.sleep(1800000);
	}

	@Test
	public void appendingAsynchronouslyIntoAMEInsertsInRightOrder() throws IOException, ExecutionException, InterruptedException {
		GoogleSheet sheet = service.getSheetByName(AME_COPY_PROD, SHEET_AME).setHeader(4);
		final String s = executor.getWithRetry(ctx -> {
			IntStream.range(1, 300).forEach(index -> {
				Row row = this.mockingAmeRow(index);
				try {
					sheet.appendRow(row)
					     .thenAccept(valueRange -> logger.info("Appended a new row to AME-> {}", valueRange.getValues()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			return "Asynchronous request finished";
		}).get();
		logger.info(s);
		Thread.sleep(1800000);
	}

	@Test
	public void appendingMultipleRows() throws IOException, ExecutionException, InterruptedException {
		GoogleSheet sheet = service.getSheetByName(BENCHMARK_ONE, SHEET_ONE).setHeader(1, 2);
		logger.info("Row count: {}", sheet.getRowCount().get());
		List rowsToAppend = new ArrayList<Row>();
		IntStream.range(1, 5000).mapToObj(this::mockingRow).forEach(rowsToAppend::add);
		final CompletableFuture appendRows = sheet.appendRows(rowsToAppend, Arrays.asList("Id"));
		final Object result = appendRows.get();
		logger.info("Results: {}", result);

	}

}
