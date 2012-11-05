package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
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
				    //If the Item was created and the Master message had the Compile flag set, 
					//the WFE will add the Item to the Compliance Logging Worklist
					AttributeMap messageAttributes = msg.getSubject();
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
						
					//TODO: Need to confirm check for already existing asset
					if (assetID == null || assetID.equals(""))
					{			
						client.createAsset(messageAttributes);
						String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

						long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.INGEST);
						AttributeMap newTask = taskController.getTask(taskID);
						newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
						taskController.saveTask(newTask);

					}
				}
			}
		};
	}
}
