package com.dj.adapter.reporting.sheets.retry.function;

import com.dj.adapter.reporting.sheets.retry.RetryContext;

@FunctionalInterface
public interface RetryRunnable {

	void run(RetryContext context) throws Exception;

}
