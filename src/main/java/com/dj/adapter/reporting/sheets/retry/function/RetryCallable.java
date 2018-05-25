package com.dj.adapter.reporting.sheets.retry.function;

import com.dj.adapter.reporting.sheets.retry.RetryContext;

@FunctionalInterface
public interface RetryCallable<V> {

	V call(RetryContext context) throws Exception;

}
