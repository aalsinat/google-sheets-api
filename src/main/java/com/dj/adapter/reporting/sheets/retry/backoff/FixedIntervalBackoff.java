package com.dj.adapter.reporting.sheets.retry.backoff;

		import com.dj.adapter.reporting.sheets.retry.RetryContext;

public class FixedIntervalBackoff implements Backoff {

	public static final long DEFAULT_PERIOD_MILLIS = 1000;

	private final long intervalMillis;

	public FixedIntervalBackoff() {
		this(DEFAULT_PERIOD_MILLIS);
	}

	public FixedIntervalBackoff(long intervalMillis) {
		this.intervalMillis = intervalMillis;
	}

	@Override
	public long delayMillis(RetryContext context) {
		return intervalMillis;
	}

}
