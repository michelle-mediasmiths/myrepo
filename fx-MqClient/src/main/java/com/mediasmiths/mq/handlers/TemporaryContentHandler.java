package com.mediasmiths.mq.handlers;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class TemporaryContentHandler  implements AttributeHandler
{
	MayamTaskController taskController;
	TasksClient client;
	private final static Logger log = Logger.getLogger(TemporaryContentHandler.class);
	
	public TemporaryContentHandler(TasksClient tasksClient, MayamTaskController controller) 
	{
		client = tasksClient;
		taskController = controller;
	}
	
	public void process(AttributeMap messageAttributes)
	{	
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		try {			
			if (assetType.equals(MayamAssetType.MATERIAL.getAssetType())) 
			{
				//TODO: Check if parent_ID has been updated, add sources to title and remove from any purge lists
				
				AttributeMap filterEqualities = client.createAttributeMap();
				filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
				filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
				FilterCriteria criteria = new FilterCriteria();
				criteria.setFilterEqualities(filterEqualities);
				FilterResult existingTasks = client.taskApi().getTasks(criteria, 10, 0);
				
				if (existingTasks.getTotalMatches() > 0) 
				{
					List<AttributeMap> tasks = existingTasks.getMatches();
					for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
					{
						AttributeMap task = tasks.get(i);
						task.setAttribute(Attribute.TASK_STATE, TaskState.C_REMOVED);
						taskController.saveTask(task);
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
				filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
				FilterCriteria criteria = new FilterCriteria();
				criteria.setFilterEqualities(filterEqualities);
				FilterResult existingTasks = client.taskApi().getTasks(criteria, 10, 0);
			
				if (existingTasks.getTotalMatches() > 0) 
				{
					List<AttributeMap> tasks = existingTasks.getMatches();
					for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
					{
						AttributeMap task = tasks.get(i);
						Calendar date = Calendar.getInstance();
						if (contentType.equals("Associated")) 
						{
							date.add(Calendar.DAY_OF_MONTH, 90);
							task.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
						}
						else if (contentType.equals("Edit Clips")) 
						{
							date.add(Calendar.DAY_OF_MONTH, 7);
							task.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
						}
						taskController.saveTask(task);
					}
				}
				else {
					long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.PURGE_CANDIDATE_LIST);
					
					AttributeMap newTask = taskController.getTask(taskID);
					newTask.putAll(messageAttributes);
					Calendar date = Calendar.getInstance();
					if (contentType.equals("Associated")) 
					{
						date.add(Calendar.DAY_OF_MONTH, 90);
						newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
					}
					else if (contentType.equals("Edit Clips")) 
					{
						date.add(Calendar.DAY_OF_MONTH, 7);
						newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
					}
					taskController.saveTask(newTask);
				}
			}
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling Temporary Content Message : "+e.getMessage(), e);
			e.printStackTrace();	
		}
	}

	@Override
	public String getName()
	{
		return "Temporary Content";
	}
}
