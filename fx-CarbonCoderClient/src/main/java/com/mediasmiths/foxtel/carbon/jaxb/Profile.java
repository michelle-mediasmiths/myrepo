package com.mediasmiths.foxtel.carbon.jaxb;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.xerces.dom.ElementNSImpl;

import com.mediasmiths.foxtel.carbon.profile.ProfileType;

@XmlRootElement(name="Profile")
public class Profile
{
	public Profile(){
		
	}
	
	public Profile(ElementNSImpl e)
	{
		setName(e.getAttribute("Name"));
		setDescription(e.getAttribute("Description"));
		setCategory(Enum.valueOf(ProfileType.class, e.getAttribute("Category")));
		setFlags(Integer.parseInt(e.getAttribute("ProfileFlags.DWD")));
		setGuid(UUID.fromString(e.getAttribute("GUID").substring(1,37))); //guids from carbon wrapped with {}
		
	}
	public String getName()
	{
		return name;
	}
	@XmlAttribute(name = "Name")
	public void setName(String name)
	{
		this.name = name;
	}
	public String getDescription()
	{
		return description;
	}
	@XmlAttribute(name = "Description")
	public void setDescription(String description)
	{
		this.description = description;
	}
	public ProfileType getCategory()
	{
		return category;
	}
	@XmlAttribute(name = "Category")
	public void setCategory(ProfileType category)
	{
		this.category = category;
	}
	public int getFlags()
	{
		return flags;
	}
	@XmlAttribute(name = "ProfileFlags.DWD")
	public void setFlags(int flags)
	{
		this.flags = flags;
	}
	
	public UUID getGuid()
	{
		return guid;
	}
	@XmlAttribute(name = "GUID")
	public void setGuid(UUID guid)
	{
		this.guid = guid;
	}
	private String name;
	private String description;
	private ProfileType category;
	private int flags;
	private UUID guid;
	
}
