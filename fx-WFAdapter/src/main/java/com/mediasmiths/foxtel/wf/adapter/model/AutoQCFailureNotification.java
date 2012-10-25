package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AutoQCFailureNotification
{
	private String assetId;
	private boolean isForTXDelivery;

	@XmlElement
	public String getAssetId()
	{
		return assetId;
	}

	public void setAssetId(String assetId)
	{
		this.assetId = assetId;
	}

	@XmlElement
	public boolean isForTXDelivery()
	{
		return isForTXDelivery;
	}

	public void setForTXDelivery(boolean isForTXDelivery)
	{
		this.isForTXDelivery = isForTXDelivery;
	}

}
