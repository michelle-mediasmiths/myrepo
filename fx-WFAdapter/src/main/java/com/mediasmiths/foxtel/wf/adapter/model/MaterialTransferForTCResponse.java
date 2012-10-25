package com.mediasmiths.foxtel.wf.adapter.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class MaterialTransferForTCResponse
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

	public MaterialTransferForTCResponse(){
		
	}
	
	public MaterialTransferForTCResponse(String destination)
	{
		this.destination=destination;
	}

}
