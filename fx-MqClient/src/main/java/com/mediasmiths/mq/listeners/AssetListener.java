package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mq.LogUtil;
import com.mediasmiths.mq.handlers.AssetDeletionHandler;
import com.mediasmiths.mq.handlers.AssetPurgeHandler;
import com.mediasmiths.mq.handlers.EmergencyIngestHandler;
import com.mediasmiths.mq.handlers.ItemCreationHandler;
import com.mediasmiths.mq.handlers.PackageUpdateHandler;
import com.mediasmiths.mq.handlers.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.UnmatchedHandler;

public class AssetListener
{
	protected final static Logger log = Logger.getLogger(AssetListener.class);
	
	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController) 
	{
		final AssetDeletionHandler assetDeletionHandler = new AssetDeletionHandler();
		final AssetPurgeHandler assetPurgeHandler = new AssetPurgeHandler();
		final EmergencyIngestHandler emergencyIngestHandler = new EmergencyIngestHandler(client, taskController);
		final ItemCreationHandler itemCreationHandler = new ItemCreationHandler(client, taskController);
		final PackageUpdateHandler packageUpdateHandler = new PackageUpdateHandler(client, taskController);
		final TemporaryContentHandler temporaryContentHandler = new TemporaryContentHandler(client, taskController);
		final UnmatchedHandler unmatchedHandler = new UnmatchedHandler(taskController);
		
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				
				log.debug(String.format("AssetListener onMessage, messagetype %s", msg.getType().toString()));
				
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					AttributeMap messageAttributes = msg.getSubject();
					
					log.trace(String.format("Attributes message: "+LogUtil.mapToString(messageAttributes)));
					
					assetDeletionHandler.process(messageAttributes);
					assetPurgeHandler.process(messageAttributes);
					emergencyIngestHandler.process(messageAttributes);
					itemCreationHandler.process(messageAttributes);
					packageUpdateHandler.process(messageAttributes);
					temporaryContentHandler.process(messageAttributes);
					unmatchedHandler.process(messageAttributes);
				}
			}

			
		};
	}
}
