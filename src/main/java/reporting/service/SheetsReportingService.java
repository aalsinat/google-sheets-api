package reporting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reporting.domain.*;

import java.io.IOException;

@Service
public class SheetsReportingService {

	private final static Integer FIRST_ROW_INDEX = 0;
	private final GoogleSheetsRepository repository;
	private final FormattingPolicy formattingPolicy;
	private final ValidationPolicy validationPolicy;

	/**
	 * Constructor
	 *
	 * @param repository
	 * @param formattingPolicy
	 * @param validationPolicy
	 */
	@Autowired
	public SheetsReportingService(GoogleSheetsRepository repository,
	                              FormattingPolicy formattingPolicy,
	                              ValidationPolicy validationPolicy) {
		this.repository = repository;
		this.formattingPolicy = formattingPolicy;
		this.validationPolicy = validationPolicy;
	}

	public FormattingPolicy getFormattingPolicy() {
		return formattingPolicy;
	}

	public ValidationPolicy getValidationPolicy() {
		return validationPolicy;
	}

	/**
	 * Returns the spreadsheet that corresponds to given identifier.
	 *
	 * @param spreadSheetId identifier of the spreadsheet to be found
	 * @return a {@code GoogleSpreadSheet} instance that represents found spreadsheet
	 * @throws IOException
	 */
	public GoogleSpreadsheet getSpreadSheetById(String spreadSheetId) throws IOException {
		return repository.getSpreadSheetById(spreadSheetId);

	}

	/**
	 * Returns the sheet with the given name.
	 *
	 * @param spreadSheetId identifier of the spreadsheet where to find the sheet
	 * @param sheetName     the name of the sheet to be found
	 * @return a {@code GoogleSheet} instance that represents found sheet
	 * @throws IOException
	 */
	public GoogleSheet getSheetByName(String spreadSheetId, String sheetName) throws IOException {
		return repository.getSpreadSheetById(spreadSheetId)
		                 .getSheetByName(sheetName);
	}
}
