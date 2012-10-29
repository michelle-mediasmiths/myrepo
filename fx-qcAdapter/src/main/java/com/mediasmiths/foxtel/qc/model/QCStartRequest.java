package com.mediasmiths.foxtel.qc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCStartRequest
{
	public String getFile()
	{
		return file;
	}
	public void setFile(String file)
	{
		this.file = file;
	}
	public String getIdent()
	{
		return ident;
	}
	public void setIdent(String ident)
	{
		this.ident = ident;
	}
	@XmlElement(name="profile")
	public String getProfileName()
	{
		return profileName;
	}
	public void setProfileName(String profileName)
	{
		this.profileName = profileName;
	}
	String file;
	String ident;
	String profileName;
}
