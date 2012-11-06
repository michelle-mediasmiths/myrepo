package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateQcHandler 
{
	public static Listener getInstance(final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//On compliance editing completion create segmentation tasks
					AttributeMap messageAttributes = msg.getSubject();
					String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
					if (taskListID.equals(MayamTaskListType.INGEST)) 
					{
						TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
						if (taskState == TaskState.OPEN) 
						{
							messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
							taskController.saveTask(messageAttributes);
							
							String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
							String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							
							MuleWorkflowController mule = new MuleWorkflowController();
							if (assetType.equals(MayamAssetType.MATERIAL.toString()))
							{
								mule.initiateQcWorkflow(assetID, false);
							} 
							else if (assetType.equals(MayamAssetType.PACKAGE.toString()))
							{
								mule.initiateQcWorkflow(assetID, true);
							}
						}
					}
				}
			}
		};
	}
}
