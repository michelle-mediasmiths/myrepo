package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class MaterialTransferForTCRequest
{
	private String packageID;
		
	@XmlElement
	public String getPackageID()
	{
		return packageID;
	}
	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
	
	@Override
	public String toString(){
		return String.format("packageID { %s } ", packageID);
	}
	
}
