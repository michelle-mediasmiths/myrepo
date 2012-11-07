package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InvokeIntalioTXFlow
{
	private String packageID;

	@XmlElement(required = true)
	public String getPackageID()
	{
		return packageID;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
}
