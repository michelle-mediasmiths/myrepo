package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCPassedNotification
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

}
