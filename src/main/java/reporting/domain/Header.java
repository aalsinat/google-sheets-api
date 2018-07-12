package reporting.domain;

import com.google.api.services.sheets.v4.model.GridRange;

import java.util.*;

public class Header {
	private final static Integer FIRST_COLUMN_INDEX = 0;
	private final static Integer FIRST_ROW_INDEX = 0;
	private final String majorDimension = "ROWS";
	private final String RANGE_NOTATION = "%s!%s";
	private final GridRange range;
	private List<Object> values;
	private Map<String, Integer> valuesMap;

	public Header(GridRange range) {
		this.range = range;
	}

	public Header(Integer rowIndex) {
		this(rowIndex, FIRST_COLUMN_INDEX);
	}

	public Header(Integer startRowIndex, Integer startColumnIndex) {
		this.range = new GridRange().setStartRowIndex(startRowIndex)
		                            .setStartColumnIndex(startColumnIndex)
		                            .setEndRowIndex(startRowIndex);
	}

	public Header(Integer startRowIndex, Integer startColumnIndex, Integer endColumnIndex) {
		this.range = new GridRange().setStartRowIndex(startRowIndex)
		                            .setStartColumnIndex(startColumnIndex)
		                            .setEndColumnIndex(endColumnIndex);
	}

	public GridRange getRange() {
		return range;
	}

	public String getRangeInA1Notation(String sheetName) {
		return String.format(RANGE_NOTATION, sheetName, getA1Notation(range));
	}

	public String getRangeInA1Notation() {
		return getA1Notation(range);
	}

	public String getStartRangeInA1Notation() {
		return getStartRangeInA1Notation(range.getStartRowIndex(), range.getStartColumnIndex());
	}

	/**
	 * Like {@code getStartRangeInA1Notation} but adding the name of the sheet.
	 *
	 * @param sheetName the name of the sheet to be added to range
	 * @return start range of the header in A1 notation, but adding the name of the sheet
	 */
	public String getStartRangeInA1Notation(String sheetName) {
		return String.format(RANGE_NOTATION, sheetName, getStartRangeInA1Notation());
	}


	public String getColumnRangeInA1Notation(String columnName) {
		final Integer columnIndex = valuesMap.get(columnName);
		if (columnIndex == null) return null;
		final GridRange columnRange = new GridRange().setStartColumnIndex(columnIndex)
		                                             .setStartRowIndex(range.getStartRowIndex() + 1)
		                                             .setEndColumnIndex(columnIndex);
		return getA1Notation(columnRange);
	}

	public String getColumnRangeInA1Notation(String columnName, String sheetName) {
		return String.format(RANGE_NOTATION, sheetName, getColumnRangeInA1Notation(columnName));
	}

	public Optional<List<Object>> getColumns() {
		return Optional.ofNullable(values);
	}

	public void setColumns(List<Object> values) {
		this.values = values;
		this.valuesMap = this.toMap();
	}

	public Integer size() {
		return values.size();
	}

	public Integer getPositionForColumn(String columnName) {
		return valuesMap.get(columnName) + range.getStartColumnIndex();
	}

	public Optional<String> getColumnByPosition(Integer position) {
		return valuesMap.entrySet()
		                .stream()
		                .filter(entry -> Objects.equals(entry.getValue(), position))
		                .map(Map.Entry::getKey)
		                .findAny();
	}

	/**
	 * Converts the list of columns to a map where the value is column's position on the list
	 **/
	public Map<String, Integer> toMap() {
		if (valuesMap != null) return valuesMap;
		valuesMap = new HashMap<>();
		values.stream()
		      .forEach(item -> valuesMap.put(String.valueOf(item), values.indexOf(item) + range.getStartColumnIndex()));
		return valuesMap;
	}

	private String convertNumToColString(int col) {
		int mod = col % 26;
		int div = col / 26;
		char small = (char) (mod + 65);
		char big = (char) (div + 64);

		return (div == 0) ? String.format("%s", small) : String.format("%s%s", big, small);
	}

	/**
	 * Given a particular range and a sheet name it returns A1 Notation representation.
	 *
	 * @param range GridRange to be converted
	 * @return String representing A1 notation for the given range
	 */
	private String getA1Notation(GridRange range) {
		final String startRange = getStartRangeInA1Notation(range.getStartRowIndex(), range.getStartColumnIndex());
		final String endRange = getEndRangeInA1Notation(range.getEndRowIndex(), range.getEndColumnIndex());
		return String.format("%s%s", startRange, endRange);
	}

	/**
	 * Support method to get A1 notation for the first part of a range.
	 *
	 * @param rowIndex    start row index of the range
	 * @param columnIndex start column index of the range
	 * @return
	 */
	private String getStartRangeInA1Notation(Integer rowIndex, Integer columnIndex) {
		final int row = rowIndex == null ? 1 : rowIndex + 1;
		final String column = columnIndex == null ? "" : convertNumToColString(columnIndex);
		return String.format("%s%d", column, row);
	}

	/**
	 * Support method to get A1 notation for the last part of the range.
	 *
	 * @param rowIndex    end row index of the range
	 * @param columnIndex end column index of the range
	 * @return
	 */
	private String getEndRangeInA1Notation(Integer rowIndex, Integer columnIndex) {
		if (rowIndex == null && columnIndex == null) {
			return "";
		}
		final String row = rowIndex == null ? "" : String.valueOf(rowIndex + 1);
		final String column = columnIndex == null ? "" : convertNumToColString(columnIndex);
		return String.format(":%s%s", column, row);
	}

}
