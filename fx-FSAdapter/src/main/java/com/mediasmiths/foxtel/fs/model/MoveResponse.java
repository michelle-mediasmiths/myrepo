package com.mediasmiths.foxtel.fs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MoveResponse extends FSAdapterResponse
{
	public MoveResponse(){
		super();
	}
	
	public MoveResponse(boolean successful)
	{
		super(successful);
	}

}
