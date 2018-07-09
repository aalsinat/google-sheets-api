package reporting.domain;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

public class Cell {
	private static final String EMPTY_STRING = "";
	private final Map.Entry<String, Object> value;

	public Cell(String column, Object value) {
		this.value = new AbstractMap.SimpleEntry<>(column, value);
	}

	public Cell(Map.Entry<String, Object> value) {
		this.value = value;
	}


	public static Cell empty(String column) {
		return new Cell(column, EMPTY_STRING);
	}

	public static Cell identity(String column) {
		return new Cell(column, null);
	}

	public String getColumn() {
		return value.getKey();
	}

	public Object getValue() {
		return value.getValue();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Cell cell = (Cell) o;
		return Objects.equals(value, cell.value);
	}

	@Override
	public int hashCode() {

		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "Cell {" +
				"value = " + value +
				" }";
	}
}
