package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCTotalFailure
{
	private String materialID;
	private String packageID;
	private boolean isTXDelivery;
	
	@XmlElement
	public String getMaterialID()
	{
		return materialID;
	}
	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}
	@XmlElement
	public String getPackageID()
	{
		return packageID;
	}
	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
	@XmlElement
	public boolean isTXDelivery()
	{
		return isTXDelivery;
	}
	public void setTXDelivery(boolean isTXDelivery)
	{
		this.isTXDelivery = isTXDelivery;
	}
}
