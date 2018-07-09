package reporting.configuration.reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.reporting.first-sheet")
public class FirstSheetConfiguration {
	private String spreadsheetId;
	private String sheetName;
	private HeaderOffset headerOffset;

	public String getSpreadsheetId() {
		return spreadsheetId;
	}

	public void setSpreadsheetId(String spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public HeaderOffset getHeaderOffset() {
		return headerOffset;
	}

	public void setHeaderOffset(HeaderOffset headerOffset) {
		this.headerOffset = headerOffset;
	}

	public static class HeaderOffset {
		private Integer startRowIndex;
		private Integer endRowIndex;
		private Integer startColumnIndex;
		private Integer endColumnIndex;

		public Integer getStartRowIndex() {
			return startRowIndex;
		}

		public void setStartRowIndex(Integer startRowIndex) {
			this.startRowIndex = startRowIndex;
		}

		public Integer getEndRowIndex() {
			return endRowIndex;
		}

		public void setEndRowIndex(Integer endRowIndex) {
			this.endRowIndex = endRowIndex;
		}

		public Integer getStartColumnIndex() {
			return startColumnIndex;
		}

		public void setStartColumnIndex(Integer startColumnIndex) {
			this.startColumnIndex = startColumnIndex;
		}

		public Integer getEndColumnIndex() {
			return endColumnIndex;
		}

		public void setEndColumnIndex(Integer endColumnIndex) {
			this.endColumnIndex = endColumnIndex;
		}
	}
}
