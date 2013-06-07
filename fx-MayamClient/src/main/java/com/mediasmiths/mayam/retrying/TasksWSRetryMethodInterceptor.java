package com.mediasmiths.mayam.retrying;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.mq.MqException;
import com.mediasmiths.foxtel.ip.common.events.CommFailure;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.std.threading.Timeout;
import com.mediasmiths.std.threading.retry.backoff.ExponentialBackoff;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

final class TasksWSRetryMethodInterceptor implements MethodInterceptor
{
	private static final Logger log = Logger.getLogger(TasksWSRetryMethodInterceptor.class);

	@Inject
	protected EventService eventsService;

	@SuppressWarnings("unchecked")
	final Class<? extends Throwable>[] retryExceptions = new Class[]{SocketException.class,
	                                                                 MqException.class,
	                                                                 SocketTimeoutException.class};

	@Inject
	@Named("tasks.retry.backofftime")
	private Long backOffTime = 1250l;
	@Inject
	@Named("tasks.retry.maxattempts")
	private Integer maxattempts = 8;
	TimeUnit backoffUnit = TimeUnit.MILLISECONDS;
	@Named("tasks.retry.exponent")
	private Double backoffExponent = 2.0d;

	@Inject
	@Named("system.events.namespace")
	private String systemEventsNamespace;


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		TasksWSRetryManager mgr = buildRetryManager();

		try
		{
			if (log.isTraceEnabled())
				log.trace("Attempting retryable invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()));

			return mgr.run(new TasksWSInvocationRetry(invocation, retryExceptions));
		}
		catch (TaskWSRetriesExhausted e)
		{
			log.warn("Reties exhausted for invoke of " +
			         invocation.getMethod().toGenericString() +
			         " on " + invocation.getThis() + " with " +
			         Arrays.asList(invocation.getArguments()) + " failed.", e);

			sendCommFailureEvent(e);

			throw e.getCause();
		}
		catch (Throwable t)
		{
			if (log.isTraceEnabled())
				log.trace("Retrying invoke of " +
				          invocation.getMethod().toGenericString() +
				          " on " + invocation.getThis() + " with " +
				          Arrays.asList(invocation.getArguments()) + " failed.", t);
			throw t;
		}
	}


	private void sendCommFailureEvent(final TaskWSRetriesExhausted e)
	{
		CommFailure cf = new CommFailure();
		cf.setFailureShortDesc("Error communicating with tasks-ws");
		cf.setFailureLongDescription(e.getCause().getLocalizedMessage());
		cf.setSource("WFE");
		cf.setTarget("tasks-ws");
		eventsService.saveEvent(systemEventsNamespace, "CommError", cf);
	}


	private TasksWSRetryManager buildRetryManager()
	{
		final Timeout initial = new Timeout(backOffTime, backoffUnit);

		ExponentialBackoff backoff = new ExponentialBackoff(initial, backoffExponent);

		return new TasksWSRetryManager(backoff, maxattempts);
	}
}
