The purpose of this library is to simplify the use of the Google Sheets API. 
## Installation


## Quickstart
The library consists mainly of three elements.
-GoogleSheetsRepository - GoogleSheetsRepository
-GoogleSpreadsheet - GoogleSpreadsheet
-GoogleSheet - GoogleSheet



### GoogleSheetsService
This class is the main wrapper for the `spreadsheets.values` collection, intended to enable the simple reading and writing of values.
#### Methods
The `spreadsheets.values` collection provides methods for reading and writing values, each with a specific task in mind:

| Range Access | Reading | Writing |
| ------------ | ------- | ------- |
|**Single range** | `spreadsheets.values.get` | `spreadsheets.values.update` |
|**Multiple ranges** | `spreadsheets.values.batchGet` | `spreadsheets.values.batchUpdate` |
|**Appending** | | `spreadsheets.values.append` |

In general, it is a good idea to combine multiple reads or updates with the batchGet and batchUpdate methods (respectively), 
as this will improve efficiency.

> Even though this methods can be called directly, it is recommended to be used through `GoogleSheet` or `GoogleSpreadsheet` classes.

Following this advice, `GoogleSheetsRepository` provides wrapper methods for:
##### `rangeSearching`
Wrapper for get method of `spreadsheets.values` collection, used for fetch a range of a particular spreadsheet.

> `ValueRange rangeSearching(String spreadSheetId, String range)`


##### `getMultipleRanges`
Wrapper for batchGet method of `spreadsheets.values` collection, used for fetching multiple ranges of a particular spreadsheet.

> `BatchGetValuesResponse getMultipleRanges(String spreadSheetId, List<String> ranges)`

##### `append`
Wrapper for append method of `spreadsheets.values` collection, used to add a new row at the end of the tab that is referenced
by the range

> `Sheets.Spreadsheets.Values.Append append(String spreadsheetId, String range, ValueRange row)`
##### `update`
Wrapper for update method of `spreadsheets.values` collection, used to update an existing row with new values.

> `Sheets.Spreadsheets.Values.Update update(String spreadsheetId, String range, ValueRange row)`


##### `getSpreadsheetById`
If a spreadsheet with that given identifier exists return the spreadsheet, otherwise return an empty one.

> `GoogleSpreadsheet getSpreadSheetById(String spreadSheetId)`

### GoogleSpreadsheet

### GoogleSheet
It behaves like a proxy for all operations related to a spreadsheet tab.
##### `getSheetId`
> `Optional<Integer> getSheetId();`
##### `getRowCount`
##### `getColumnCount`
##### `setHeaderRow`
##### `setHeaderOffset`
##### `getHeader`
##### `getRowIdByColumnValues`
##### `getRowById`
##### `appendRow`
##### `saveRow`
##### `updateRow`
