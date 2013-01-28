package com.mediasmiths.foxtel.pathresolver;

public class UnknownPathException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnknownPathException(String message)
	{
		super(message);
	}

	public UnknownPathException(Throwable cause)
	{
		super(cause);
	}

	public UnknownPathException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
