package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;

public abstract class AutoQCRequest
{
	private String assetId;
	private boolean isForTXDelivery;

	@XmlElement(required=true)
	public String getAssetId()
	{
		return assetId;
	}

	public void setAssetId(String assetId)
	{
		this.assetId = assetId;
	}

	@XmlElement(required=true)
	public boolean isForTXDelivery()
	{
		return isForTXDelivery;
	}

	public void setForTXDelivery(boolean isForTXDelivery)
	{
		this.isForTXDelivery = isForTXDelivery;
	}

}