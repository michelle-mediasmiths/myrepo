package com.mediasmiths.mq.handlers;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;

public class TemporaryContentHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(TemporaryContentHandler.class);
	
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{	
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
		
		
		try {			
			if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()) && attributeChanged(Attribute.PARENT_HOUSE_ID, before, after)) 
			{
				//Remove from any purge candidate lists
				AttributeMap filterEqualities = tasksClient.createAttributeMap();
				filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
				filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
				FilterCriteria criteria = new FilterCriteria();
				criteria.setFilterEqualities(filterEqualities);
				FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
				
				if (existingTasks.getTotalMatches() > 0) 
				{
					List<AttributeMap> tasks = existingTasks.getMatches();
					for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
					{
						AttributeMap task = tasks.get(i);
						task.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
						taskController.saveTask(task);
					}
				}
			}
			else if (attributeChanged(Attribute.CONT_CATEGORY, before, after))
			{
				// - Content Type changed to “Associated” - Item added to Purge candidate if not already, expiry date set as 90 days
				// - Content Type set to "Edit Clips" - Item added to purge list if not already there and expiry set for 7 days
				String contentType = currentAttributes.getAttribute(Attribute.CONT_CATEGORY);
				if (contentType.equals("Associated") || contentType.equals("Edit Clips")) 
				{
					AttributeMap filterEqualities = tasksClient.createAttributeMap();
					filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
					filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
				
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
								task.setAttribute(Attribute.OP_DATE, date.getTime());
							}
							else if (contentType.equals("Edit Clips")) 
							{
								date.add(Calendar.DAY_OF_MONTH, 7);
								task.setAttribute(Attribute.OP_DATE, date.getTime());
							}
							taskController.saveTask(task);
						}
					}
					else {
						long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.PURGE_CANDIDATE_LIST);
						
						AttributeMap newTask = taskController.getTask(taskID);
						newTask.putAll(currentAttributes);
						Calendar date = Calendar.getInstance();
						if (contentType.equals("Associated")) 
						{
							date.add(Calendar.DAY_OF_MONTH, 90);
							newTask.setAttribute(Attribute.OP_DATE, date.getTime());
						}
						else if (contentType.equals("Edit Clips")) 
						{
							date.add(Calendar.DAY_OF_MONTH, 7);
							newTask.setAttribute(Attribute.OP_DATE, date.getTime());
						}
						taskController.saveTask(newTask);
					}
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
