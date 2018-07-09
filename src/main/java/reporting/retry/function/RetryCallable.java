package reporting.retry.function;

import reporting.retry.RetryContext;

@FunctionalInterface
public interface RetryCallable<V> {

	V call(RetryContext context) throws Exception;

}
