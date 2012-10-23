package com.mediasmiths.foxtel.wf.adapter.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class MaterialTransferForQCRequest
{
	private String materialID;
		
	@XmlElement
	public String getMaterialID()
	{
		return materialID;
	}
	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}
	
	@Override
	public String toString(){
		return String.format("materialID { %s } ", materialID);
	}
	
}
