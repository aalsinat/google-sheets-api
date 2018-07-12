package reporting.domain;

import org.springframework.util.MultiValueMap;
import reporting.utils.MultiValuedMapCollector;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Row {
	private List<Cell> values;
	private FormattingPolicy formattingPolicy;
	private ValidationPolicy validationPolicy;
	private Predicate<Object> additionPolicy;
	private Map<String, String> definition;

	private Row(RowBuilder builder) {
		this.formattingPolicy = builder.formattingPolicy;
		this.validationPolicy = builder.validationPolicy;
		this.additionPolicy = builder.additionPolicy;
		this.definition = builder.definition;
		this.values = builder.values;
	}

	public static RowBuilder builder() {
		return new RowBuilder();
	}

	/**
	 * Support method for change the name of a cell column, based on a provided dictionary.
	 *
	 * @param source      name of the cell column to be changed
	 * @param translation dictionary used to translate
	 * @return translated name of the cell column
	 */
	private String translate(String source, Map<String, String> translation) {
		String translated = translation.get(source);
		return (translated == null) ? source : translated;
	}

	/**
	 * Given a map of possible columns and values, creates cells and add them to the row.
	 * 1. Adds extra cells if an addition policy and a column definition has been set
	 * 2. Adds cells for every given value
	 * 3. Translate cell columns if a column definition has been set
	 *
	 * @param rowValues map of columns and values.
	 * @return {@code Row} itself with added and translated cells
	 */
	public Row addCells(Map<String, Object> rowValues) {
		this.addExtraCells();
		rowValues.entrySet()
		         .stream()
		         .forEach(value -> addCell(value.getKey(), value.getValue()));
		this.translate();
		return this;
	}


	/**
	 * Whether an addition policy and a definition has been established, this method
	 * adds all columns on the definition that passes addition policy test.
	 */
	private void addExtraCells() {
		if ((additionPolicy != null) && (definition != null)) {
			definition.entrySet()
			          .stream()
			          .filter(column -> additionPolicy.test(column.getValue()))
			          .forEach(column -> addCell(column.getKey(), column.getValue()));
		}
	}

	/**
	 * Adds a new cell to the row.
	 *
	 * @param column name of the column
	 * @param value  value of the cell
	 * @return row itself with added cell
	 */
	public Row addCell(String column, Object value) {
		Cell newCell = new Cell(column, getFormatted(column, getValidated(column, value)));
		values.add(newCell);
		return this;
	}

	/**
	 * Support method that applies formatting policies to column and value pairs.
	 *
	 * @param column name of the cell
	 * @param value  value of the cell
	 * @return value once policy has been applied
	 */
	private Object getFormatted(String column, Object value) {
		return formattingPolicy.apply(column, value);
	}

	/**
	 * Support method that applies validation policies to column and value pairs.
	 *
	 * @param column
	 * @param value
	 * @return
	 */
	private Object getValidated(String column, Object value) {
		return validationPolicy.apply(column, value);
	}

	/**
	 * Returns first cell containing provided column name.
	 *
	 * @param columnName name of the column of the cell to be found
	 * @return cell containing provided column name
	 */
	public Cell findCellByColumnName(String columnName) {
		return findCellByColumnName(columnName, Cell::empty);
	}


	/**
	 * Returns first cell containing provided column name, otherwise the result of applying the provided function
	 *
	 * @param columnName   name of the column of the cell to be found
	 * @param defaultValue function that provides a default value in case that none of the cells contains columnName
	 * @return cell containing provided column name
	 */
	public Cell findCellByColumnName(String columnName, Function<String, Cell> defaultValue) {
		Optional<Cell> cellToBeSearched = values.stream().filter(cell -> Objects.equals(cell.getColumn(), columnName))
		                                        .findFirst();
		if (cellToBeSearched.isPresent()) {
			return cellToBeSearched.get();
		} else {
			return defaultValue.apply(columnName);
		}
	}

	public List<Cell> findCellsByColumnNames(List<String> columnNames) {
		return columnNames.stream().map(columnName -> this.findCellByColumnName(columnName, Cell::empty))
		                  .collect(Collectors.toList());
	}


	/**
	 * Returns a list of {@linl Cell}s containing provided column names.
	 *
	 * @param columnNames  name of the column of the cell to be found
	 * @param defaultValue function that provides a default value in case that none of the cells contains columnName
	 * @return list of cells containing provided column names
	 */
	public List<Cell> findCellsByColumnNames(List<String> columnNames, Function<String, Cell> defaultValue) {
		return columnNames.stream().map(columnName -> this.findCellByColumnName(columnName, defaultValue))
		                  .collect(Collectors.toList());
	}

	/**
	 * Returns first cell containing provided value.
	 *
	 * @param value value of the cell to be found
	 * @return cell containing provided value
	 */
	public Optional<Cell> findCellByValue(Object value) {
		return values.stream().filter(cell -> Objects.equals(cell.getValue(), value)).findFirst();
	}

	/**
	 * Sets what theoretical cell columns can be added to this row.
	 *
	 * @param definition map of column names and variables
	 */
	public Row withColumnDefinition(Map<String, String> definition) {
		this.definition = definition;
		return this;
	}


	/**
	 * Creates a list of values, mixing a header definition and the values of the {@code Row} itself.
	 * If the column of the header is present in the list of {@link Cell}s of the {@code Row} it is added
	 * to the final list, otherwise is not part of the result.
	 *
	 * @param header header definition of a {@link GoogleSheet}
	 * @return
	 */
	public List<Object> merge(Header header) {
		return IntStream
				.range(header.getRange().getStartColumnIndex(), header.getRange().getStartColumnIndex() + header.size())
				.mapToObj(header::getColumnByPosition)
				.filter(Optional::isPresent)
				.map(columnName -> findCellByColumnName(columnName.get(), Cell::empty))
				.map(Cell::getValue)
				.map(Object::toString)
				.collect(Collectors.toList());
	}

	/**
	 * Creates a list of values, mixing a header definition and a list of values with the values of the {@code Row} itself.
	 * If the column of the header is present in the list of {@link Cell}s of the {@code Row} it is added
	 * to the final list, otherwise the value provided with the list is the one that is added.
	 *
	 * @param header header definition of a {@link GoogleSheet}
	 * @param values list of values that will be updated with the ones on the {@code Row}
	 * @return
	 */
	public List<Object> merge(Header header, List<List<Object>> values) {
		final Map<String, Integer> headerPositions = header.toMap();
		return IntStream.range(header.getRange().getStartColumnIndex(), header.size())
		                .mapToObj(header::getColumnByPosition)
		                .filter(Optional::isPresent)
		                .map(Optional::get)
		                .map(columnName -> findCellByColumnName(columnName, column -> {
			                Integer position = headerPositions.get(column);
			                if (position >= values.get(0).size()) {
				                return Cell.empty(column);
			                } else {
				                return new Cell(column, values.get(0).get(position));
			                }
		                }))
		                .map(Cell::getValue)
		                .map(Object::toString)
		                .collect(Collectors.toList());
	}

	/**
	 * Given a particular columns definition translates column names for all cells.
	 */
	private void translate() {
		if (definition != null) {
			MultiValueMap<String, Object> dictionary = reverse(definition);
			final List<Cell> translatedRow = new ArrayList<>();
			for (Cell value : values) {
				List<Object> captions = dictionary.get(value.getColumn());
				if (captions == null) {
					translatedRow.add(new Cell(value.getColumn(), value.getValue()));
				} else {
					captions.stream()
					        .forEach(caption -> translatedRow.add(new Cell(caption.toString(), value.getValue())));
				}
			}
			this.values = translatedRow;
		}
	}

	/**
	 * Converts a map of cell column names to task variables into a multivalue map.
	 *
	 * @param source map containing cell column names anf its corresponding task variables
	 * @return
	 */
	private MultiValueMap<String, Object> reverse(Map<String, String> source) {
		return source.entrySet()
		             .stream()
		             .collect(MultiValuedMapCollector.toMultivaluedMap(o -> (String) ((Map.Entry) o).getValue(),
		                                                               o -> ((Map.Entry) o).getKey()));

	}


	/**
	 * Given a column name, returns true if any {@link Cell} of the {@code Row} has that column amongst its values.
	 *
	 * @param columnName the name of the column to be checked
	 * @return true if the columns is in the list of values of a row
	 */
	public boolean containsColumn(String columnName) {
		return (!findCellByColumnName(columnName).equals(Cell.empty(columnName)));
	}


	/**
	 * Converts the list of {@link Cell}s that represents the {@Row } to a map of values,
	 * having every key the name of the column, and every value the content of the cell.
	 *
	 * @return map of key/values, where the key is the column of the cell and the value is the content of the cell
	 */
	public Map<String, Object> toMap() {
		return values.stream()
		             .map(cell -> new AbstractMap.SimpleEntry<>(cell.getColumn(), cell.getValue()))
		             .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Applying Builder pattern for creating new {@code Row}s.
	 */
	public static class RowBuilder implements Builder<Row> {
		private List<Cell> values;
		private FormattingPolicy formattingPolicy;
		private ValidationPolicy validationPolicy;
		private Map<String, String> definition;
		private Predicate<Object> additionPolicy;

		public RowBuilder() {
			this.values = new ArrayList<>();
		}

		/**
		 * Set a validation policy for row cells.
		 *
		 * @param policy validation policy to be set.
		 * @return row itself with added policy
		 */
		public RowBuilder withValidationPolicy(ValidationPolicy policy) {
			validationPolicy = policy;
			return this;
		}

		/**
		 * Set a formatting policy for row cells.
		 *
		 * @param policy formatting policy to be set.
		 * @return row itself with added policy
		 */
		public RowBuilder withFormattingPolicy(FormattingPolicy policy) {
			formattingPolicy = policy;
			return this;
		}

		/**
		 * Sets a predicate that defines what extra cell must be added to the row, even if the are not passed as
		 * arguments. This predicate is applied to every single column definition.
		 *
		 * @param policy a predicate that defines what column definition must be added.
		 * @return
		 */
		public RowBuilder withAdditionPolicy(Predicate<Object> policy) {
			this.additionPolicy = policy;
			return this;
		}

		/**
		 * Calls the private constructor of the Row class and passes itself as the argument.
		 *
		 * @return a {@code Row} instantiated with the parameters set by the {@code RowBuilder}
		 */
		public Row build() {
			return new Row(this);
		}
	}
}
