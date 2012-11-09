package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlElement;

public abstract class FSAdapterResponse
{
	protected boolean successful;
	
	public FSAdapterResponse(){
		
	}
	
	public FSAdapterResponse(boolean successful)
	{
		this.successful = successful;
	}

	@XmlElement(required = true)
	public boolean isSuccessful()
	{
		return successful;
	}

	public void setSuccessful(boolean successful)
	{
		this.successful = successful;
	}
}
