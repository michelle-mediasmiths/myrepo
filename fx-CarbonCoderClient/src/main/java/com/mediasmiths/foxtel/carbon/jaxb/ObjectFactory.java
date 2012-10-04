package com.mediasmiths.foxtel.carbon.jaxb;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory
{
	public ObjectFactory()
	{
	}

	public CarbonReply createCarbonReply()
	{
		return new CarbonReply();
	}
}