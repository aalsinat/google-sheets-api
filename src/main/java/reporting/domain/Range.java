package reporting.domain;

import com.google.api.services.sheets.v4.model.GridRange;

public class Range {
	private String sheetTitle;
	private Integer startRow;
	private Integer endRow;
	private Integer startColumn;
	private Integer endColumn;
	private GridRange range;

	private Range(Builder builder) {
		this.sheetTitle = builder.sheetTitle;
		this.startRow = builder.startRow;
		this.endRow = builder.endRow;
		this.startColumn = builder.startColumn;
		this.endColumn = builder.endColumn;
		this.range = (builder.range == null) ? this.fromRange() : builder.range;
	}

	private GridRange fromRange() {
		return new GridRange().setStartRowIndex(this.startRow).setEndRowIndex(this.endRow)
		                      .setStartColumnIndex(this.startColumn).setEndColumnIndex(this.endColumn);
	}

	/**
	 * Returns a new instance of {@link Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}

	public String getRangeInA1Notation() {
		final String startRange = getUpperLeftRangeNotation(this.startRow, this.startColumn);
		final String endRange = getBottomRightRangeNotation(this.endRow, this.endColumn);
		return String.format("%s!%s%s", this.sheetTitle, startRange, endRange);
	}

	/**
	 * Support method to get A1 notation for the first part of the range.
	 *
	 * @param rowIndex    start row index of the range
	 * @param columnIndex start column index of the range
	 * @return
	 */
	private String getUpperLeftRangeNotation(Integer rowIndex, Integer columnIndex) {
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
	private String getBottomRightRangeNotation(Integer rowIndex, Integer columnIndex) {
		if (rowIndex == null && columnIndex == null) return "";
		final String row = rowIndex == null ? "" : String.valueOf(rowIndex + 1);
		final String column = columnIndex == null ? "" : convertNumToColString(columnIndex);
		return String.format(":%s%s", column, row);
	}

	/**
	 * Converts a number to a sheet column name. Starting from index 0.
	 *
	 * @param col number to be converted
	 * @return the name of the column corresponding to given parameter
	 */
	private String convertNumToColString(int col) {
		int mod = col % 26;
		int div = col / 26;
		char small = (char) (mod + 65);
		char big = (char) (div + 64);

		return (div == 0) ? String.format("%s", small) : String.format("%s%s", big, small);
	}

	public static class Builder {
		private String sheetTitle;
		private Integer startRow;
		private Integer endRow;
		private Integer startColumn;
		private Integer endColumn;
		private GridRange range;

		public Builder() {
		}

		public Builder(String sheetTitle, GridRange range) {
			this.range = range;
		}

		public Builder(String sheetTitle, Integer startRow) {
			this.sheetTitle = sheetTitle;
			this.startRow = startRow;
		}


		public Builder withSheetTitle(String sheetTitle) {
			this.sheetTitle = sheetTitle;
			return this;
		}

		public Builder withStartingRow(Integer startRow) {
			this.startRow = startRow;
			return this;
		}

		public Builder withEndingRow(Integer endRow) {
			this.endRow = endRow;
			return this;
		}

		public Builder withStartingColumn(Integer startColumn) {
			this.startColumn = startColumn;
			return this;
		}

		public Builder withEndingColumn(Integer endColumn) {
			this.endColumn = endColumn;
			return this;
		}

		public Builder withRange(GridRange range) {
			this.range = range;
			return this;
		}

		/**
		 * Calls the private constructor of the Range class and passes itself as the argument.
		 *
		 * @return a {@code Range} instantiated with the parameters set by the {@code Builder}
		 */
		public Range build() {
			return new Range(this);
		}
	}
}
