package reporting.retry;


import reporting.retry.function.RetryCallable;
import reporting.retry.function.RetryRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public interface RetryExecutor {

	CompletableFuture<Void> doWithRetry(RetryRunnable action);

	<V> CompletableFuture<V> getWithRetry(Callable<V> task);

	<V> CompletableFuture<V> getWithRetry(RetryCallable<V> task);

	<V> CompletableFuture<V> getFutureWithRetry(RetryCallable<CompletableFuture<V>> task);
}
