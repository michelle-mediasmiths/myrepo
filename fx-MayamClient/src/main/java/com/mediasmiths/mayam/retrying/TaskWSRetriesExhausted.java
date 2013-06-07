package com.mediasmiths.mayam.retrying;

public class TaskWSRetriesExhausted extends RuntimeException
{
	public TaskWSRetriesExhausted(final String message,final Throwable cause)
	{
		super(message,cause);
	}
}
