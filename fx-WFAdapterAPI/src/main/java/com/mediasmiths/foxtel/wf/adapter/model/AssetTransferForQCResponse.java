package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class AssetTransferForQCResponse
{

	private String destination;
	
	@XmlElement
	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public AssetTransferForQCResponse(){
		
	}
	
	public AssetTransferForQCResponse(String destination)
	{
		this.destination=destination;
	}

}
