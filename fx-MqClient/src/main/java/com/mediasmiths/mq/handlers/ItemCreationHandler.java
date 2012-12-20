package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;

public class ItemCreationHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(ItemCreationHandler.class);
	
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
			// Material Ingest and Comp Logging tasks are created by MaterialController due to need to set Required By Date which is not mapped in AGLs
			if (!assetType.equals(MayamAssetType.MATERIAL.getAssetType())) 
			{
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
