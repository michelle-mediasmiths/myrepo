package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SelectMostRecentResponse extends FSAdapterResponse
{
	public SelectMostRecentResponse()
	{
		super();
	}

	public SelectMostRecentResponse(boolean successful)
	{
		super(successful);
	}

}
