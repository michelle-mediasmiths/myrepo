package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeleteResponse extends FSAdapterResponse
{
	public DeleteResponse()
	{
		super();
	}

	public DeleteResponse(boolean successful)
	{
		super(successful);
	}

}
