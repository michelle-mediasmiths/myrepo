package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqContentType;
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

public class AssetListener {
	protected final static Logger log = Logger.getLogger(AssetListener.class);

	public static Listener getInstance(final TasksClient client,
			final MayamTaskController taskController) {
		final AssetDeletionHandler assetDeletionHandler = new AssetDeletionHandler();
		final AssetPurgeHandler assetPurgeHandler = new AssetPurgeHandler();
		final EmergencyIngestHandler emergencyIngestHandler = new EmergencyIngestHandler(
				client, taskController);
		final ItemCreationHandler itemCreationHandler = new ItemCreationHandler(
				client, taskController);
		final PackageUpdateHandler packageUpdateHandler = new PackageUpdateHandler(
				client, taskController);
		final TemporaryContentHandler temporaryContentHandler = new TemporaryContentHandler(
				client, taskController);
		final UnmatchedHandler unmatchedHandler = new UnmatchedHandler(
				taskController);

		return new Listener() {
			public void onMessage(MqMessage msg) throws Throwable {

				log.debug(String.format(
						"AssetListener onMessage, messagetype %s", msg
								.getType().toString()));

				MqContentType type = msg.getType();
				if(type==null){
					log.warn("Message has a null type");
				}
				else{
					
					if (type.equals(ContentTypes.ATTRIBUTES)) {
						AttributeMap messageAttributes = msg.getSubject();
	
						log.trace(String.format("Attributes message: "
								+ LogUtil.mapToString(messageAttributes)));
						try {
							assetDeletionHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in asset deletion handler", e);
						}
						try {
							assetPurgeHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in asset purge handler", e);
						}
						try {
							emergencyIngestHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in emergency ingest handler", e);
						}
						try {
							itemCreationHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in item creation handler", e);
						}
						try {
							packageUpdateHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in package update handler", e);
						}
						try {
							temporaryContentHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in temporary content handler", e);
						}
						try {
							unmatchedHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in unmatched handler", e);
						}
					}
					else{
						log.trace("Message not of type ATTRIBUTES, ignoring");
					}
				}
			}

		};
	}
}
