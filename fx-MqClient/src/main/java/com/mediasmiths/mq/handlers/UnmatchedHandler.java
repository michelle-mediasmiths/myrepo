package com.mediasmiths.mq.handlers;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class UnmatchedHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{
		String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
		if (contentFormat != null && contentFormat.equals("Unmatched")) 
		{
			try {
				//Add to purge candidate list with expiry date of 30 days
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.PURGE_CANDIDATE_LIST);
				
				//Initiate QC flow
				MuleWorkflowController mule = new MuleWorkflowController();
				if (assetType.equals(MayamAssetType.MATERIAL.getText()))
				{
					mule.initiateQcWorkflow(assetID, false);
				} 
				else if (assetType.equals(MayamAssetType.PACKAGE.getText()))
				{
					mule.initiateQcWorkflow(assetID, true);
				}
				
				AttributeMap newTask = taskController.getTask(taskID);
				newTask.putAll(messageAttributes);
				Calendar date = Calendar.getInstance();
				date.add(Calendar.DAY_OF_MONTH, 30);
				newTask.setAttribute(Attribute.MEDIA_EXPIRES, date.getTime());
				taskController.saveTask(newTask);
				
				//Add to Ingest Worklist
				long qcTaskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.INGEST);
				AttributeMap ingestTask = taskController.getTask(qcTaskID);
				ingestTask.putAll(messageAttributes);
				taskController.saveTask(ingestTask);
				
				//Add to Unmatched worklist
				long unmatchedTaskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.UNMATCHED_MEDIA);
				AttributeMap unmatchedTask = taskController.getTask(unmatchedTaskID);
				unmatchedTask.putAll(messageAttributes);
				taskController.saveTask(unmatchedTask);	
			}
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling Unmatched Content Message : ", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Unmatched";
	}
}
