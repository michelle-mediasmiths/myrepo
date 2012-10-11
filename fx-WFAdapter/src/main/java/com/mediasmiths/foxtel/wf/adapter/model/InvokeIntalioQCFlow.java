package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class InvokeIntalioQCFlow
{
	private String materialID;
	private boolean forTXDelivery;

	@XmlElement(required=true)
	public String getMaterialID()
	{
		return materialID;
	}

	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}

	@XmlElement(required=true)
	public boolean isforTXDelivery()
	{
		return forTXDelivery;
	}

	public void setforTXDelivery(boolean isQcForTXDelivery)
	{
		this.forTXDelivery = isQcForTXDelivery;
	}
}
