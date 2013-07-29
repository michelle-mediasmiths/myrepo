package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CleanupResponse extends FSAdapterResponse
{
	public CleanupResponse()
	{
		super();
	}

	public CleanupResponse(boolean successful)
	{
		super(successful);
	}

}
