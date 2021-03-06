package com.dj.adapter.reporting.sheets.retry.backoff;

		import com.dj.adapter.reporting.sheets.retry.RetryContext;

public interface Backoff {

	Backoff DEFAULT = new FixedIntervalBackoff();

	long delayMillis(RetryContext context);

	default Backoff withUniformJitter() {
		return new UniformRandomBackoff(this);
	}

	default Backoff withUniformJitter(long range) {
		return new UniformRandomBackoff(this, range);
	}

	default Backoff withProportionalJitter() {
		return new ProportionalRandomBackoff(this);
	}

	default Backoff withProportionalJitter(double multiplier) {
		return new ProportionalRandomBackoff(this, multiplier);
	}

	default Backoff withMinDelay(long minDelayMillis) {
		return new BoundedMinBackoff(this, minDelayMillis);
	}

	default Backoff withMinDelay() {
		return new BoundedMinBackoff(this);
	}

	default Backoff withMaxDelay(long maxDelayMillis) {
		return new BoundedMaxBackoff(this, maxDelayMillis);
	}

	default Backoff withMaxDelay() {
		return new BoundedMaxBackoff(this);
	}

	default Backoff withFirstRetryNoDelay() {
		return new FirstRetryNoDelayBackoff(this);
	}
}
