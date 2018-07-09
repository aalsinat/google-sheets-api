package reporting.retry.backoff;


import reporting.retry.AsyncRetryContext;
import reporting.retry.RetryContext;

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
