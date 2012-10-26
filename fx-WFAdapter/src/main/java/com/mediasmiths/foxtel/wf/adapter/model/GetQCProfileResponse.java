package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetQCProfileResponse
{

	private String profile;

	@XmlElement
	public String getProfile()
	{
		return profile;
	}

	public void setProfile(String profile)
	{
		this.profile = profile;
	}
	
	
	
}
