package com.mediasmiths.foxtel.tc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCBuildJobXMLRequest
{
	String packageID;

	@XmlElement(name="packageID")
	public String getPackageID()
	{
		return packageID;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
}
