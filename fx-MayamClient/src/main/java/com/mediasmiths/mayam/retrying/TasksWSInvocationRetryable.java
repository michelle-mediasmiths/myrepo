package com.mediasmiths.mayam.retrying;

import java.util.EnumSet;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import com.mediasmiths.std.threading.retry.Retryable;

final class TasksWSInvocationRetryable implements Retryable<Object>
{
	private static final Logger log = Logger.getLogger(TasksWSInvocationRetryable.class);

	final MethodInvocation invocation;
	final Class<? extends Throwable>[] retryExceptions;

	public TasksWSInvocationRetryable(MethodInvocation invocation,
	                           final Class<? extends Throwable>[] retryExceptions)
	{
		this.invocation = invocation;
		this.retryExceptions = retryExceptions;
	}


	@Override
	public Object attempt(final int attempt) throws Exception
	{
		try
		{
			if (log.isTraceEnabled())
				log.trace("Invoking " + this.toString() + " for attempt #" + attempt);

			return invocation.proceed();
		}
		catch (Exception e)
		{
			throw e;
		}
		catch (Error e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean shouldRetry(final int attempt, final Throwable e)
	{
		// Throw if the type is in retryConditions
		for (Class<? extends Throwable> type : retryExceptions)
		{
			if (containsException(e,type))
				return true;
		}
		
		return false; //by default dont retry
	}

	
	private boolean containsException(Throwable t, Class<?> exceptionClass)
	{
		if (t == null)
			return false;
		if (t.getClass() == exceptionClass)
			return true;
		return containsException(t.getCause(), exceptionClass);
	}
	  
	@Override
	public String toString()
	{
		return invocation.getMethod().toString();
	}
}
