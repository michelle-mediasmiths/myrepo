package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class ItemCreationListener 
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
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
						
					//TODO: Need to confirm check for already existing asset
					if (assetID == null || assetID.equals(""))
					{			
						String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
						String parentID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
						
						if (assetType.equals(AssetType.ITEM) && (parentID != null)) 
						{
							//Skip QC and move straight to compliance	
							//Compile flag is not stored in Mayam, but Parent Id is taken from compiled object
							//As such we are using the presence of Parent Id on a Material asset to determined if compile was set
							long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.COMPLIANCE_LOGGING);
							AttributeMap newTask = taskController.getTask(taskID);
							newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							taskController.saveTask(newTask);
						}
						else {
							long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.INGEST);
							AttributeMap newTask = taskController.getTask(taskID);
							newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							taskController.saveTask(newTask);
						}
					}
				}
			}
		};
	}
}
