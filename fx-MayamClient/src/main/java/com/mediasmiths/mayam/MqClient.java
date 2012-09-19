package com.mediasmiths.mayam;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.inject.Injector;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.mq.common.Queues;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.FilterResult;
import com.mediasmiths.mayam.controllers.MayamTaskController;


public class MqClient {
	private final Mq mq;
	private ArrayList<Detachable> listeners;
	private TasksClient client;
	private MayamTaskController taskController;
	
	public MqClient(Injector injector, TasksClient mayamClient, MayamTaskController mayamTaskController) 
	{
		mq = injector.getInstance(Mq.class);
		listeners = new ArrayList<Detachable>();
		mq.listen(ListenIntensity.NORMAL);
		client = mayamClient;
		taskController = mayamTaskController;
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
				String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
	
				//TODO: Confirm the actual value of the Unmatched field
				if (contentFormat.equals("Unmatched")) {
//						TODO: Initiate QC workflow
				
//						Set ACLs for temporary item
				
//						Add to purge candidate list with expiry date of 30 days
						String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
						String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
						long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.PURGE_CANDIDATE_LIST);
						
						AttributeMap newTask = client.getTask(taskID);
						newTask.putAll(messageAttributes);
						Calendar date = Calendar.getInstance();
						date.add(Calendar.DAY_OF_MONTH, 30);
						newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
						client.updateTask(newTask);
				}
			}
		}
	};
	
	private Listener assetDeletionListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Deletion has occurred in Viz Ardome, close all related workflow tasks - DG: Mayam or us?
				// - How to tell if an asset is deleted?
			}
		}
	};
	
	private Listener assetPurgeListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				//TODO: IMPLEMENT
				// - Purge of temporary assets notification received, remove from other worklist
				// - How to tell if an asset is ready to be purged? Check the Expiry date?
			}
		}
	};
	
	private Listener emergencyIngestListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				AttributeMap messageAttributes = msg.getSubject();
				
				//TODO: IMPLEMENT
				// - Emergency ingest - ACLS updated in Ardome - 
				// Check if asset exists
				// If not then create placeholder for it
				// How do we check if the content already exists if we dont have an ID for it?
				
				client.createAsset(messageAttributes);
			}
		}
	};
	
	private Listener temporaryContentListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
				AttributeMap messageAttributes = msg.getSubject();

				// - Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
				String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
				
				if (assetType.equals("ITEM")) 
				{
					//TODO: Check if parent_ID has been updated, add sources to title and remove from any purge lists
					
					AttributeMap filterEqualities = client.createAttributeMap();
					filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
					filterEqualities.setAttribute(Attribute.ASSET_ID, assetID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = client.getTasks(criteria, 10, 0);
					
					if (existingTasks.getTotalMatches() > 0) {
						List<AttributeMap> tasks = existingTasks.getMatches();
						for (int i = 0; i < existingTasks.getTotalMatches(); i++) {
							AttributeMap task = tasks.get(i);
							task.setAttribute(Attribute.TASK_STATE, TaskState.C_REMOVED);
							client.updateTask(task);
						}
					}
				}
				
				
				// - Content Type changed to “Associated” - Item added to Purge candidate if not already, expiry date set as 90 days
				// - Content Type set to "Edit Clips" - Item added to purge list if not already there and expiry set for 7 days
				String contentType = messageAttributes.getAttribute(Attribute.CONT_CATEGORY);
				if (contentType.equals("Associated") || contentType.equals("Edit Clips")) 
				{
					AttributeMap filterEqualities = client.createAttributeMap();
					filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
					filterEqualities.setAttribute(Attribute.ASSET_ID, assetID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = client.getTasks(criteria, 10, 0);
					
					if (existingTasks.getTotalMatches() > 0) {
						List<AttributeMap> tasks = existingTasks.getMatches();
						for (int i = 0; i < existingTasks.getTotalMatches(); i++) {
							AttributeMap task = tasks.get(i);
							Calendar date = Calendar.getInstance();
							if (contentType.equals("Associated")) {
								date.add(Calendar.DAY_OF_MONTH, 90);
								task.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
							}
							else if (contentType.equals("Edit Clips")) {
								date.add(Calendar.DAY_OF_MONTH, 7);
								task.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
							}
							client.updateTask(task);
						}
					}
					else {
						long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.PURGE_CANDIDATE_LIST);
						
						AttributeMap newTask = client.getTask(taskID);
						newTask.putAll(messageAttributes);
						Calendar date = Calendar.getInstance();
						if (contentType.equals("Associated")) {
							date.add(Calendar.DAY_OF_MONTH, 90);
							newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
						}
						else if (contentType.equals("Edit Clips")) {
							date.add(Calendar.DAY_OF_MONTH, 7);
							newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
						}
						client.updateTask(newTask);
					}
				}

				
			}
		}
	};
	
	// Listen for a completed Segmentation task - Change the asset to TX-Ready - then start tx task and kick off workflow
	private Listener segmentationCompleteListener = new Listener() {
		public void onMessage(MqMessage msg) throws Throwable {
			System.out.println(msg.getContent());
			if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {

				AttributeMap messageAttributes = msg.getSubject();
				String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
				if (taskListID.equals(MayamTaskListType.SEGMENTATION)) {
					TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
					if (taskState == TaskState.FINISHED) {
						messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
						client.updateTask(messageAttributes);
						
						String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
						String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
						long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.TX_DELIVERY);
						AttributeMap newTask = client.getTask(taskID);
						newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
						client.updateTask(newTask);
						
						//TODO: Initiate workflow
						
					}
				}
				
			}
		}
	};
}
