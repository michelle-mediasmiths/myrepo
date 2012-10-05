package com.mediasmiths.foxtel.carbonwfs;

public class WfsClientException extends Exception
{

	public enum reason{
		WORKFLOW_CONSTRUCTION_FROM_TEMPLATE_FAILED;
	}
	
	private static final long serialVersionUID = 1L;

	private WfsClientException(){
		//hide the default constructor, make people pass a message or cause!
	}
	
	public WfsClientException(WfsClientException.reason reason){
		this(reason.toString());
	}
	
	public WfsClientException(WfsClientException.reason reason, Throwable cause){
		this(reason.toString(), cause);
	}
	
	public WfsClientException(String message)
	{
		super(message);
	}

	public WfsClientException(String message, Throwable cause){
		super(message,cause);
	}
	
	public WfsClientException(Throwable cause){
		super(cause);
	}
	
}
