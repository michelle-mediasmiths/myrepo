package com.mediasmiths.mayam.retrying;
import com.mediasmiths.std.threading.retry.RetryManager;
import com.mediasmiths.std.threading.retry.Retryable;
import com.mediasmiths.std.threading.retry.backoff.BackoffStrategy;
import org.apache.log4j.Logger;

public class TasksWSRetryManager extends RetryManager
{
	private static final Logger log = Logger.getLogger(TasksWSRetryManager.class);

	public TasksWSRetryManager(final BackoffStrategy strategy, final int maxAttempts)
	{
		super(strategy, maxAttempts);
	}

	@Override
	protected <T> T finalAttemptFailed(final Retryable<T> operation, final int attempt, final Throwable e) throws Exception
	{
		if (maxAttemptsReached(attempt))
		{
			log.error("Final attempt #" + attempt + " of " + operation + " failed.", e);
			throw new TaskWSRetriesExhausted("Failed after " + attempt + " retries",e);
		}
		else
		{
			return super.finalAttemptFailed(operation, attempt, e);
		}
	}
}
