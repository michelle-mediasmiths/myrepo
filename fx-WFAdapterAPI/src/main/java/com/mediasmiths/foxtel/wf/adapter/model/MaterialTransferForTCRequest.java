package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class MaterialTransferForTCRequest
{
	private String assetID;
	private boolean isForTX;	
	
	@XmlElement(required=true)
	public boolean isForTX()
	{
		return isForTX;
	}
	public void setForTX(boolean isForTX)
	{
		this.isForTX = isForTX;
	}
	@XmlElement(required=true)
	public String getAssetID()
	{
		return assetID;
	}
	public void setAssetID(String assetID)
	{
		this.assetID = assetID;
	}
	
	@Override
	public String toString(){
		return String.format("assetID { %s } ",assetID);
	}
	
}
