package reporting.retry.function;

import reporting.retry.RetryContext;

@FunctionalInterface
public interface RetryRunnable {

	void run(RetryContext context) throws Exception;

}
