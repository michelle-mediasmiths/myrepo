package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.jaxws.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class FixAndStitchListener 
{
	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//On compliance logging completion create compliance editing task
					AttributeMap messageAttributes = msg.getSubject();
					String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
					if (taskListID.equals(MayamTaskListType.FIX_STITCH_EDIT)) 
					{
						TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
						if (taskState == TaskState.FINISHED) 
						{
							messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
							client.updateTask(messageAttributes);
								
							String assetID = messageAttributes.getAttribute(Attribute.ASSET_GUID);
							String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							
							String parentID = "";
							//TODO: Parent ID not yet implemented
							//parentID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
							
							//Compile flag is not stored in Mayam, but Parent Id is taken from compiled object
							//As such we are using the presence of Parent Id on a Material asset to determined if compile was set
							if (assetType.equals(AssetType.ITEM) && (parentID != null || parentID.equals(""))) 
							{
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.COMPLIANCE_LOGGING);
								AttributeMap newTask = client.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								client.updateTask(newTask);
							}
							else {
								//If not eligible for compliance then skip straight to segmentation
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.SEGMENTATION);
								AttributeMap newTask = client.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								client.updateTask(newTask);
							}
						}	
					}

				}
			}
		};
	}
}
