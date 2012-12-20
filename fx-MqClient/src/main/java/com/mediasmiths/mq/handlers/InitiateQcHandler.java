package com.mediasmiths.mq.handlers;


import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateQcHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(InitiateQcHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.QC_VIEW.getText())) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.OPEN) 
			{
				try {
					
					try{
					messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(messageAttributes);
					}
					catch(Exception e){
						log.error("error updating task state",e);
					}
							
					String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
					AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							
					MuleWorkflowController mule = new MuleWorkflowController();
					
					log.info("Initiating qc workflow for asset " + assetID);
					
					if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
					{
						mule.initiateQcWorkflow(assetID, false);
					} 
					else if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
					{
						mule.initiateQcWorkflow(assetID, true);
					}
				}
				catch (Exception e) {
					log.error("Exception in the Mayam client while handling Inititae QC Message : ",e);
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
