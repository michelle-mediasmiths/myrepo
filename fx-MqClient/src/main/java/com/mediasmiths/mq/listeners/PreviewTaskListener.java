package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class PreviewTaskListener 
{
	public static Listener getInstance(final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//Listen for preview tasks completion, create editing tasks as appropriate
					AttributeMap messageAttributes = msg.getSubject();
					String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
					if (taskListID.equals(MayamTaskListType.PREVIEW)) 
					{
						TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
						if (taskState == TaskState.FINISHED) 
						{
							messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
							taskController.saveTask(messageAttributes);
								
							String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
							String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.FIX_STITCH_EDIT);
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
