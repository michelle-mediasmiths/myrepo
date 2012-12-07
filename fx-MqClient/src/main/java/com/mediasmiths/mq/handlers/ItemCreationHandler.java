package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class ItemCreationHandler  implements AttributeHandler
{
	MayamTaskController taskController;
	TasksClient client;
	private final static Logger log = Logger.getLogger(ItemCreationHandler.class);
	
	public ItemCreationHandler(TasksClient tasksClient, MayamTaskController controller) 
	{
		client = tasksClient;
		taskController = controller;
	}
	
	
	/**
	 * To only be called in response to changeType CREATE messages
	 */
	public void process(AttributeMap messageAttributes)
	{	
		String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
					
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		String parentID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
		
		log.debug(String.format("assetID %s parentID %s",assetID,parentID));
		
		try {
			if (assetType.equals(AssetType.ITEM) && (parentID != null)) 
			{
				//Skip QC and move straight to compliance	
				//Compile flag is not stored in Mayam, but Parent Id is taken from compiled object
				//As such we are using the presence of Parent Id on a Material asset to determined if compile was set
				long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.COMPLIANCE_LOGGING);
				log.debug("created task with id : "+taskID);
				AttributeMap newTask = taskController.getTask(taskID);
				newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
				taskController.saveTask(newTask);
			}
			else {
				long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.INGEST);
				log.debug("created task with id : "+taskID);
				AttributeMap newTask = taskController.getTask(taskID);
				newTask.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
				taskController.saveTask(newTask);
			}
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling Item Creation Message : " + e.getMessage(),e);
		}
	}

	@Override
	public String getName()
	{
		return "Item creation";
	}
}
