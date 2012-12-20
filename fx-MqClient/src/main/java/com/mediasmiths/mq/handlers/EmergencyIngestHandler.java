package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.exception.RemoteException;

public class EmergencyIngestHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(EmergencyIngestHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{	
		// Check if asset exists
		// If not then create placeholder for it
		// How do we check if the content already exists if we dont have an ID for it?	
		String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);		
		if (assetID == null || assetID.equals(""))
		{			
			try {
				tasksClient.assetApi().createAsset(messageAttributes);
			} catch (RemoteException e) {
				log.error("Exception thrown by Mayam will creating new asset from emergency ingest : ", e);
			}
		}		
	}

	@Override
	public String getName()
	{
		return "Emergency Ingest";
	}
}
