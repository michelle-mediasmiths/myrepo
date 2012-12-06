package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mq.LogUtil;
import com.mediasmiths.mq.handlers.AssetDeletionHandler;
import com.mediasmiths.mq.handlers.AssetPurgeHandler;
import com.mediasmiths.mq.handlers.EmergencyIngestHandler;
import com.mediasmiths.mq.handlers.Handler;
import com.mediasmiths.mq.handlers.ItemCreationHandler;
import com.mediasmiths.mq.handlers.PackageUpdateHandler;
import com.mediasmiths.mq.handlers.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.UnmatchedHandler;

public class AssetListener {
	protected final static Logger log = Logger.getLogger(AssetListener.class);

	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController, EventService eventService) 
	{
		final AssetDeletionHandler assetDeletionHandler = new AssetDeletionHandler();
		final AssetPurgeHandler assetPurgeHandler = new AssetPurgeHandler();
		final EmergencyIngestHandler emergencyIngestHandler = new EmergencyIngestHandler(client, taskController);
		final ItemCreationHandler itemCreationHandler = new ItemCreationHandler(client, taskController);
		final PackageUpdateHandler packageUpdateHandler = new PackageUpdateHandler(client, taskController);
		final TemporaryContentHandler temporaryContentHandler = new TemporaryContentHandler(client, taskController);
		final UnmatchedHandler unmatchedHandler = new UnmatchedHandler(taskController);
		
		return new MqClientListener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{	
				
				if(msg.getType() == null){
					log.debug(String.format("AssetListener onMessage, messagetype is null"));
				}
				else{
					log.debug(String.format("AssetListener onMessage, messagetype %s", msg.getType().type()));
				}
				
				String origin = msg.getProperties().get(MqMessage.PROP_ORIGIN_DESTINATION);
				
				log.trace("origin is:"+origin);
				
				if (msg.getType() != null && origin != null) 
				{
					if (msg.getType().equals(ContentTypes.ATTRIBUTES) && origin.contains("asset"))
					{
						AttributeMap messageAttributes = msg.getSubject();
						
						log.trace(String.format("Attributes message: "+LogUtil.mapToString(messageAttributes)));

						passEventToHandler(assetDeletionHandler, messageAttributes);
						passEventToHandler(assetPurgeHandler,messageAttributes);
						passEventToHandler(emergencyIngestHandler,messageAttributes);
						passEventToHandler(itemCreationHandler,messageAttributes);
						passEventToHandler(packageUpdateHandler,messageAttributes);
						passEventToHandler(temporaryContentHandler,messageAttributes);
						passEventToHandler(unmatchedHandler,messageAttributes);
					}
					else{
						log.debug("Message is not of types ATTRIBUTES, ignoring");
					}
				}
				else {
					log.debug(String.format("AssetListener onMessage, type or origin was null. Msg: %s", msg.toString()));
				}
			}
			
		};
		
	}
}
