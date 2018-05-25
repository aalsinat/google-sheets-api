package com.dj.adapter.reporting.sheets.retry.backoff;

import java.util.Objects;

public abstract class BackoffWrapper implements Backoff {

	protected final Backoff target;

	public BackoffWrapper(Backoff target) {
		this.target = Objects.requireNonNull(target);
	}
}
