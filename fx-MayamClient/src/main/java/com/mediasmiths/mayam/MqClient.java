package com.mediasmiths.mayam;
import java.util.ArrayList;

import com.google.inject.Injector;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.mq.common.Queues;


public class MqClient {
	private final Mq mq;
	private ArrayList<Detachable> listeners;
	
	public MqClient(Injector injector) 
	{
		mq = injector.getInstance(Mq.class);
		listeners = new ArrayList<Detachable>();
		mq.listen(ListenIntensity.NORMAL);
	}
		
	public void dispose()
	{
		for (int i=0; i < listeners.size(); i++)
		{
			listeners.get(i).detach();
		}
		mq.shutdownConsumers();
		mq.shutdownProducers();
	}
	
	public void setListenIntensity(ListenIntensity intensity)
	{
		mq.listen(intensity);
	}
	
	public void attachListener(MqDestination type, Listener listener)
	{
		Detachable mqListener = mq.attachListener(type, listener);
		listeners.add(mqListener);
	}
	
	//TODO: Other expected listeners?
	// - QC button clicked, update QC flag - DG: Shouldnt this by Mayam?
	public void attachIncomingListners() 
	{
		attachListener(Queues.MAM_INCOMING, unMatchedListener);
		attachListener(Queues.MAM_INCOMING, assetDeletionListener);
		attachListener(Queues.MAM_INCOMING, assetPurgeListener);
		attachListener(Queues.MAM_INCOMING, emergencyIngestListener);
		attachListener(Queues.MAM_INCOMING, temporaryContentListener);
		attachListener(Queues.MAM_INCOMING, segmentationCompleteListener);
	}
	
	public MayamClientErrorCode sendMessage(MqDestination destination, MqMessage message) throws MayamClientException
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			mq.send(destination, message);
		} catch (MqException e) {
			returnCode = MayamClientErrorCode.MQ_MESSAGE_SEND_FAILED;
			throw new MayamClientException(returnCode);
		}
		return returnCode;
	}
	
	private Listener unMatchedListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				AttributeMap messageAttributes = msg.getSubject();
				//TODO: Handle incoming attribute messages - check content format for unmatched media
				String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
	
//				if (contentFormat.equals("Unmatched")) {
//						
//				}
			}
		}
	};
	
	private Listener assetDeletionListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Deletion has occurred in Viz Ardome, close all related workflow tasks - DG: Mayam or us?
			}
		}
	};
	
	private Listener assetPurgeListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Purge of temporary assets notification received, remove from other worklist
			}
		}
	};
	
	private Listener emergencyIngestListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Emergency ingest - ACLS updated in Ardome - 

			}
		}
	};
	
	private Listener temporaryContentListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
				// - Content Type changed to “Associated” - Item added to Purge candidate if not already, expiry date set as 90 days
				// - Content Type set to "Edit Clips" - Item added to purge list if not already there and expiry set for 7 days
			}
		}
	};
	
	private Listener segmentationCompleteListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Segmentation task complete - TX-Ready - then start tx task and kick off workflow
			}
		}
	};
}
