package com.dj.adapter.reporting.sheets;

import com.dj.adapter.reporting.sheets.model.GoogleSheet;
import com.dj.adapter.reporting.sheets.model.GoogleSpreadsheet;
import com.dj.adapter.reporting.sheets.service.GoogleSheetsService;
import com.dj.adapter.reporting.sheets.service.ServiceFactory;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SheetsApplicationTests {
    final String spreadsheetId = "1A-C_yPt34w3fX2ZUxveH2qpPoiCVDQ7L7a9MdwXvCBY";

    @Autowired
    private ServiceFactory serviceFactory;

    @Autowired
    private GoogleSheetsService sheetsService;

    @Test
    public void contextLoads() {
    }

    @Test
    public void sheetsServiceTest() {
        assertNotNull(serviceFactory);
        assertNotNull("A new Sheets service has been created", sheetsService);
        assertTrue("Service has right application name", sheetsService.get()
                                                                      .getApplicationName() == "FlowIT Reporting");
    }

    @Test
    public void spreadSheetTest() throws IOException {
        final GoogleSpreadsheet testSpreadSheet = sheetsService.getSpreadSheetById(spreadsheetId);
        assertTrue("Found by id spreadsheet exists", testSpreadSheet.isPresent());
    }

    @Test
    public void sheetTest() throws IOException {
        final GoogleSpreadsheet testSpreadSheet = sheetsService.getSpreadSheetById(spreadsheetId);
        /*
         * Testing search a sheet by its name
         */
        final GoogleSheet testSheet = testSpreadSheet.getSheetByName("Class Data");
        assertTrue("A sheet with that name exists", testSheet.isPresent());

        /*
         * Check Id property
         */
        final Optional<Integer> sheetId = testSheet.getSheetId();
        assertTrue("Found sheet has an Id", sheetId.isPresent());
        assertTrue("Sheet identifier must be non-negative", sheetId.get() >= 0);

        /*
         * Check RowCount property
         */
        final Optional<Integer> rowCount = testSheet.getRowCount();
        assertTrue("The number of rows in the grid is non-negative", rowCount.get() > 0);
        System.out.println(String.format("Number of rows in the grid: %d", rowCount.get()));

        /*
         * Check ColumnCount property
         */
        final Optional<Integer> columnCount = testSheet.getColumnCount();
        assertTrue("The number of columns in the grid is non-negative", columnCount.get() > 0);
        System.out.println(String.format("Number of columns in the grid: %d", columnCount.get()));

        /*
         * Get and Set header properties
         */
        final Map<Object, Integer> header = testSheet.setHeaderRow(2).getHeader();
        assertTrue("Header exists,", header != null);
        assertTrue("Number of columns for header is 7", (header != null) && header.size() == 7);
        System.out.println(header);
        testSheet.setHeaderOffset(new GridRange().setStartRowIndex(2).setStartColumnIndex(2));

        /*
         * Testing row insertion
         */
        final List<Object> newRow = Arrays.asList(String.valueOf(rowCount), "Alex Alsina", "Male", "4. Senior", "BCN", "Catalan", "Basketball");
        com.google.api.services.sheets.v4.model.ValueRange inserted = testSheet.save(newRow);
        System.out.println(String.format("Inserted values: %s", inserted.toPrettyString()));

        /*
         * Testing find row by multiple columns
         */

        final Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("Student Name", "Benjamin");
        searchCriteria.put("Gender", "Male");
        final Optional<Integer> rowIdByColumnValues = testSheet.findRowIdByColumnValues(searchCriteria);

    }

}
