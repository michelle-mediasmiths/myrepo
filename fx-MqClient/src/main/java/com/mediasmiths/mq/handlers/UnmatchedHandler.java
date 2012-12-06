package com.mediasmiths.mq.handlers;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class UnmatchedHandler  implements AttributeHandler
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(UnmatchedHandler.class);
	
	public UnmatchedHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(AttributeMap messageAttributes)
	{
		String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
		if (contentFormat.equals("Unmatched")) 
		{
			try {
				//TODO : identify origin of unmatched content (emergency ingest\ DART ) 
			
				//Add to purge candidate list with expiry date of 30 days
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.PURGE_CANDIDATE_LIST);
				
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
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling Unmatched Content Message : " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getName()
	{
		return "Unmatched";
	}
}
