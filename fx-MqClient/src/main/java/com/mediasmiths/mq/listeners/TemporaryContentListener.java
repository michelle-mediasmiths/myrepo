package com.mediasmiths.mq.listeners;

import java.util.Calendar;
import java.util.List;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class TemporaryContentListener 
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

					// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
					String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_GUID);
					
					if (assetType.equals("ITEM")) 
					{
						//TODO: Check if parent_ID has been updated, add sources to title and remove from any purge lists
						
						AttributeMap filterEqualities = client.createAttributeMap();
						filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
						filterEqualities.setAttribute(Attribute.ASSET_GUID, assetID);
						FilterCriteria criteria = new FilterCriteria();
						criteria.setFilterEqualities(filterEqualities);
						FilterResult existingTasks = client.getTasks(criteria, 10, 0);
						
						if (existingTasks.getTotalMatches() > 0) 
						{
							List<AttributeMap> tasks = existingTasks.getMatches();
							for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
							{
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
						filterEqualities.setAttribute(Attribute.ASSET_GUID, assetID);
						FilterCriteria criteria = new FilterCriteria();
						criteria.setFilterEqualities(filterEqualities);
						FilterResult existingTasks = client.getTasks(criteria, 10, 0);
						
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
								client.updateTask(task);
							}
						}
						else {
							long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.PURGE_CANDIDATE_LIST);
							
							AttributeMap newTask = client.getTask(taskID);
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
							client.updateTask(newTask);
						}
					}	
				}
			}
		};
	}
}
