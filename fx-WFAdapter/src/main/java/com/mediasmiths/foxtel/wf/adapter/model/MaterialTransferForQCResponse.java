package com.mediasmiths.foxtel.wf.adapter.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlRootElement
public class MaterialTransferForQCResponse
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

	public MaterialTransferForQCResponse(){
		
	}
	
	public MaterialTransferForQCResponse(String destination)
	{
		this.destination=destination;
	}

}
