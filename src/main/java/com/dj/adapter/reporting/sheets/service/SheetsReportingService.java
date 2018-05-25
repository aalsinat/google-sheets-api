package com.dj.adapter.reporting.sheets.service;

import com.dj.adapter.reporting.sheets.domain.GoogleSheetsRepository;
import com.dj.adapter.reporting.sheets.domain.GoogleSheet;
import com.dj.adapter.reporting.sheets.domain.GoogleSpreadsheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SheetsReportingService {

	private final GoogleSheetsRepository repository;

	@Autowired
	public SheetsReportingService(GoogleSheetsRepository repository) {
		this.repository = repository;
	}

	public GoogleSpreadsheet getSpreadSheetById(String spreadSheetId) throws IOException {
		return repository.getSpreadSheetById(spreadSheetId);

	}

	public GoogleSheet getSheetByName(String spreadSheetId, String sheetName) throws IOException {
		return repository.getSpreadSheetById(spreadSheetId)
		                 .getSheetByName(sheetName);
	}
}
