package com.mediasmiths.foxtel.wf.adapter.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlRootElement
public class MaterialTransferForQCResponse
{

	private URI destination;
	
	@XmlElement
	public URI getDestination()
	{
		return destination;
	}

	public void setDestination(URI destination)
	{
		this.destination = destination;
	}

	public MaterialTransferForQCResponse(){
		
	}
	
	public MaterialTransferForQCResponse(URI destination)
	{
		this.destination=destination;
	}

}
