package com.mediasmiths.foxtel.carbon.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

import org.apache.xerces.dom.ElementNSImpl;

public class ProfileList
{
	private List<ElementNSImpl> profiles;

	public List<ElementNSImpl> getProfiles()
	{
		return profiles;
	}
	@XmlAnyElement(lax = true)
	public void setProfiles(List<ElementNSImpl> profiles)
	{
		this.profiles = profiles;

	}
}
