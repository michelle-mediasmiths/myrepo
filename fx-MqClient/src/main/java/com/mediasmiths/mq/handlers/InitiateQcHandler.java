package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateQcHandler  implements Handler
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(InitiateQcHandler.class);
	
	public InitiateQcHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.INGEST)) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.OPEN) 
			{
				try {
					messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(messageAttributes);
							
					String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
					AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							
					MuleWorkflowController mule = new MuleWorkflowController();
					if (assetType.toString().equals(MayamAssetType.MATERIAL.toString()))
					{
						mule.initiateQcWorkflow(assetID, false);
					} 
					else if (assetType.toString().equals(MayamAssetType.PACKAGE.toString()))
					{
						mule.initiateQcWorkflow(assetID, true);
					}
				}
				catch (Exception e) {
					log.error("Exception in the Mayam client while handling Inititae QC Message : " + e);
					e.printStackTrace();			
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Initiate QC";
	}
}
