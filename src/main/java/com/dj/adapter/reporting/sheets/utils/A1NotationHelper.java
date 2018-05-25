package com.dj.adapter.reporting.sheets.utils;

import com.google.api.services.sheets.v4.model.GridRange;

public class A1NotationHelper {
	/**
	 * Given a particular range and a sheet name it returns A1 Notation representation.
	 *
	 * @param range GridRange to be converted
	 * @return String representing A1 notation for the given range
	 */
	public static String getNotationFromSheetNameAndGridRange(String sheetName, GridRange range) {
		final String startRange = getNotationFromStartRangeIndexes(range.getStartRowIndex(), range.getStartColumnIndex());
		final String endRange = getNotationFromEndRangeIndexes(range.getEndRowIndex(), range.getEndColumnIndex());
		return String.format("%s!%s%s", sheetName, startRange, endRange);
	}

	/**
	 * Auxiliar method to get A1 notation for the first part of a range.
	 *
	 * @param rowIndex    start row index of the range
	 * @param columnIndex start column index of the range
	 * @return
	 */
	private static String getNotationFromStartRangeIndexes(Integer rowIndex, Integer columnIndex) {
		final int row = rowIndex == null ? 1 : rowIndex + 1;
		final String column = columnIndex == null ? "" : convertNumToColString(columnIndex);
		return String.format("%s%d", column, row);
	}

	/**
	 * Auxiliar method to get A1 notation for the last part of the range.
	 *
	 * @param rowIndex    end row index of the range
	 * @param columnIndex end column index of the range
	 * @return
	 */
	private static String getNotationFromEndRangeIndexes(Integer rowIndex, Integer columnIndex) {
		if (rowIndex == null && columnIndex == null) {
			return "";
		}
		final String row = rowIndex == null ? "" : String.valueOf(rowIndex + 1);
		final String column = columnIndex == null ? "" : convertNumToColString(columnIndex);
		return String.format(":%s%s", column, row);
	}

	/**
	 * Auxiliar method to get A1 notation for last part of the range
	 *
	 * @param range          the range the values cover, in GridRange format
	 * @param majorDimension the major dimension of the values
	 * @return A1 notation for last part of the range
	 */
	private static String getNotationForEndIndexes(GridRange range, String majorDimension) {
		String row, column;
		if (majorDimension.equals("ROWS")) {
			row = range.getEndRowIndex() == null ?
			      String.valueOf(range.getStartRowIndex() + 1) :
			      String.valueOf(range.getEndRowIndex() + 1);
			column = range.getEndColumnIndex() == null ? "" : convertNumToColString(range.getEndColumnIndex());
		} else {
			row = range.getEndRowIndex() == null ? "" : String.valueOf(range.getEndRowIndex() + 1);
			column = range.getEndColumnIndex() == null ?
			         convertNumToColString(range.getStartColumnIndex()) :
			         convertNumToColString(range.getEndColumnIndex());
		}
		return String.format("%s%s", column, row);
	}

	/**
	 * Converts a number to a sheet column name. Starting from index 0.
	 *
	 * @param col number to be converted
	 * @return the name of the column corresponding to given parameter
	 */
	public static String convertNumToColString(int col) {
		String columnName = null;
		int mod = col % 26;
		int div = col / 26;
		char small = (char) (mod + 65);
		char big = (char) (div + 64);

		return (div == 0) ? String.format("%s", small) : String.format("%s%s", big, small);

//		if (div == 0) {
//			columnName = "" + small;
//		} else {
//			columnName = "" + big + "" + small;
//		}
//
//		return columnName;
	}

	/**
	 * Converts a column name of a sheet to a number, starting from index 0.
	 *
	 * @param ref column name to be converted.
	 * @return the number corresponding to the given column name
	 */
	public static int convertColStringToNum(String ref) {
		int len = ref.length();
		int retval = 0;
		int pos = 0;

		for (int k = ref.length() - 1; k > -1; k--) {
			char thechar = ref.charAt(k);
			if (pos == 0) {
				retval += (Character.getNumericValue(thechar) - 9);
			} else {
				retval += (Character.getNumericValue(thechar) - 9)
						* (pos * 26);
			}
			pos++;
		}
		return retval - 1;
	}

}
