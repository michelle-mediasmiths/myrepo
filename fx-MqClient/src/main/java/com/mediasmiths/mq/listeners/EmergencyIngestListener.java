package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class EmergencyIngestListener 
{
	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					AttributeMap messageAttributes = msg.getSubject();
			
					//TODO: IMPLEMENT
					// - Emergency ingest - ACLS updated in Ardome 
					// Check if asset exists
					// If not then create placeholder for it
					// How do we check if the content already exists if we dont have an ID for it?
					
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_GUID);
					
					if (assetID == null || assetID.equals(""))
					{			
						client.createAsset(messageAttributes);
					}
				}
			}
		};
	}
}
