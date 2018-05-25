package com.dj.adapter.reporting.sheets.retry.backoff;

		import com.dj.adapter.reporting.sheets.retry.AsyncRetryContext;
		import com.dj.adapter.reporting.sheets.retry.RetryContext;

public class FirstRetryNoDelayBackoff extends BackoffWrapper {

	public FirstRetryNoDelayBackoff(Backoff target) {
		super(target);
	}

	@Override
	public long delayMillis(RetryContext context) {
		if (context.isFirstRetry()) {
			return 0;
		} else {
			return target.delayMillis(decrementRetryCount(context));
		}
	}

	private RetryContext decrementRetryCount(RetryContext context) {
		return ((AsyncRetryContext) context).prevRetry();
	}

}
