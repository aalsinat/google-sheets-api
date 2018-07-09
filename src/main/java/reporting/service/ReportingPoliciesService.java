package reporting.service;

import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Service
public class ReportingPoliciesService {
	public static final int CELL_MAX_LENGTH = 49999;

	/**
	 * Formatting policy to convert Date variables coming from task request to string.
	 */
	public static Object dateToStringFormat(String field, Object value) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		df.setTimeZone(timeZone);

		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return df.format((Date) value);
		} else {
			return value.toString();
		}
	}

	/**
	 * Formatting policy to add value to meta variables coming from task request.
	 */
	public static Object metaVariableForCurrentDateFormat(String field, Object value) {
		if ("_current_date".equals(value)) {
			TimeZone timeZone = TimeZone.getTimeZone("UTC");
			return Calendar.getInstance(timeZone)
			               .getTime();
		} else {
			return value;
		}
	}

	/**
	 * Validation policy to verify that no null values exist.
	 */
	public static Object checkForNullValues(String field, Object value) {
		return value == null ? "" : value;
	}

	/**
	 * Validation policy to make sure that the cell value does not exceed the maximum length.
	 */
	public static Object checkMaximumLength(String field, Object value) {
		if ((value instanceof String) && ((String) value).length() > CELL_MAX_LENGTH) {
			return ((String) value).substring(0, CELL_MAX_LENGTH);
		} else {
			return value;
		}
	}

	/**
	 * Defines what a meta-variable is for a cell value.
	 *
	 * @param value cell value to be evaluated
	 * @return
	 */
	public static Boolean isMetaVariableValue(Object value) {
		return ((value instanceof String) && (((String) value).startsWith("_")));
	}
}
