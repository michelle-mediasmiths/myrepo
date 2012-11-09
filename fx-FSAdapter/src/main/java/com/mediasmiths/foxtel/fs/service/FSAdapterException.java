package com.mediasmiths.foxtel.fs.service;

public class FSAdapterException extends Exception
{

	private static final long serialVersionUID = 1L;

	public FSAdapterException(String message)
	{
		super(message);
	}
	
	public FSAdapterException(String message, Throwable cause){
		super(message,cause);
	}

}
