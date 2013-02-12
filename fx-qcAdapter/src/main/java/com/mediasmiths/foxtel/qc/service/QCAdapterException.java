package com.mediasmiths.foxtel.qc.service;


public class QCAdapterException extends Exception
{
	private static final long serialVersionUID = 1L;

	private QCAdapterException(){
		//hide the default constructor, make people pass a message or cause!
	}
	
	public QCAdapterException(String message)
	{
		super(message);
	}

	public QCAdapterException(String message, Throwable cause){
		super(message,cause);
	}
	
	public QCAdapterException(Throwable cause){
		super(cause);
	}
	
}
