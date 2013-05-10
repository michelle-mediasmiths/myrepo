package com.mediasmiths.mayam.retrying;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import com.mayam.wf.mq.MqException;
import com.mediasmiths.std.threading.Timeout;
import com.mediasmiths.std.threading.retry.RetryManager;
import com.mediasmiths.std.threading.retry.backoff.ExponentialBackoff;

final class TasksWSRetryMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(TasksWSRetryMethodInterceptor.class);

	@SuppressWarnings("unchecked")
	final Class<? extends Throwable>[] retryExceptions = new Class []{SocketException.class, MqException.class, SocketTimeoutException.class};
	
	private long backOffTime = 1000;	
	private int maxattempts = 8;	
	TimeUnit backoffUnit = TimeUnit.MILLISECONDS;
	double backoffExponent = 2.0d;
	
	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		RetryManager mgr = buildRetryManager();

		try
		{
			if (log.isDebugEnabled())
				log.debug("Attempting retryable invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()));

			return mgr.run(new TasksWSInvocationRetry(invocation, retryExceptions));
		}
		catch (Throwable t)
		{
			if (log.isDebugEnabled())
				log.debug("Retrying invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()) + " failed.", t);

			throw t;
		}
	}

	private RetryManager buildRetryManager()
	{
		final Timeout initial = new Timeout(backOffTime, backoffUnit);

		ExponentialBackoff backoff = new ExponentialBackoff(initial, backoffExponent);

		return new RetryManager(backoff, maxattempts);
	}
}
