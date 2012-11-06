package com.mediasmiths.mq.listeners;

import java.util.Calendar;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class UnmatchedListener 
{
	public static Listener getInstance(final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					AttributeMap messageAttributes = msg.getSubject();
					String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
		
					//TODO: Confirm the actual value of the Unmatched field
					if (contentFormat.equals("Unmatched")) 
					{
						//TODO : identify origin of unmatched content (emergency ingest\ DART ) 
						
							//Add to purge candidate list with expiry date of 30 days
							String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
							long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.PURGE_CANDIDATE_LIST);
							
							MuleWorkflowController mule = new MuleWorkflowController();
							if (assetType.equals(MayamAssetType.MATERIAL.toString()))
							{
								mule.initiateQcWorkflow(assetID, false);
							} 
							else if (assetType.equals(MayamAssetType.PACKAGE.toString()))
							{
								mule.initiateQcWorkflow(assetID, true);
							}
							
							AttributeMap newTask = taskController.getTask(taskID);
							newTask.putAll(messageAttributes);
							Calendar date = Calendar.getInstance();
							date.add(Calendar.DAY_OF_MONTH, 30);
							newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
							taskController.saveTask(newTask);
					}
				}
			}
		};
	}
}
