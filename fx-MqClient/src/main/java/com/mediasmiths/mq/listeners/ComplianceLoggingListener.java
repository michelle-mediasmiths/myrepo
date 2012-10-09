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

public class ComplianceLoggingListener 
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
					if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
					{
						AttributeMap messageAttributes = msg.getSubject();
						String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
						if (taskListID.equals(MayamTaskListType.COMPLIANCE_LOGGING)) 
						{
							TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
							if (taskState == TaskState.FINISHED) 
							{
								messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
								client.updateTask(messageAttributes);
								
								String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
								String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.COMPLIANCE_EDIT);
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
