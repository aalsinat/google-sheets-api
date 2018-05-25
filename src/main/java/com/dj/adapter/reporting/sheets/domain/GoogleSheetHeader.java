package com.dj.adapter.reporting.sheets.domain;

import com.dj.adapter.reporting.sheets.utils.GoogleSheetsUtils;
import com.google.api.services.sheets.v4.model.GridRange;

import java.util.List;
import java.util.Map;

public class GoogleSheetHeader {
	private final static Integer FIRST_COLUMN_INDEX = 0;
	private final static Integer FIRST_ROW_INDEX = 0;
	private final String majorDimension = "ROWS";
	private final GridRange range;
	private List<Object> values;

	public GoogleSheetHeader(GridRange range) {
		this.range = range;
	}

	public GoogleSheetHeader(Integer rowIndex) {
		this(rowIndex, FIRST_COLUMN_INDEX);
	}

	public GoogleSheetHeader(Integer startRowIndex, Integer startColumnIndex) {
		this.range = new GridRange().setStartRowIndex(startRowIndex)
		                            .setStartColumnIndex(startColumnIndex)
		                            .setEndRowIndex(startRowIndex);
	}

	public GoogleSheetHeader(Integer startRowIndex, Integer startColumnIndex, Integer endColumnIndex) {
		this.range = new GridRange().setStartRowIndex(startRowIndex)
		                            .setStartColumnIndex(startColumnIndex)
		                            .setEndColumnIndex(endColumnIndex);
	}

	public String getRangeInA1Notation() {
		final int startRow = range.getStartRowIndex() == null ? 1 : range.getStartRowIndex() + 1;
		final String startColumn = range.getStartColumnIndex() == null ? "" : convertNumToColString(
				range.getStartColumnIndex());
		final String startRange = String.format("%s%d", startColumn, startRow);

		final String endRow = range.getEndRowIndex() == null ?
		                      String.valueOf(range.getStartRowIndex() + 1) :
		                      String.valueOf(range.getEndRowIndex() + 1);
		final String endColumn = range.getEndColumnIndex() == null ? "" : convertNumToColString(range.getEndColumnIndex());
		final String endRange = String.format("%s%s", endColumn, endRow);

		return String.format("%s:%s", startRange, endRange);
	}

	public List<Object> getColumns() {
		return values;
	}

	public Map<String, Integer> getColumnRangeInA1Notation() {
		return GoogleSheetsUtils.listToMap(values, range.getStartColumnIndex());
	}

	public Integer getColumnPosition(String columnName) {
		return values.indexOf(columnName) + range.getStartColumnIndex();
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	private String convertNumToColString(int col) {
		int mod = col % 26;
		int div = col / 26;
		char small = (char) (mod + 65);
		char big = (char) (div + 64);

		return (div == 0) ? String.format("%s", small) : String.format("%s%s", big, small);
	}
}
