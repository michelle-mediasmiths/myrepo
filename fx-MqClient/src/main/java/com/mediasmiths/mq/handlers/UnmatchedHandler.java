package com.mediasmiths.mq.handlers;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;

public class UnmatchedHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{
		String contentFormat = messageAttributes.getAttribute(Attribute.CONT_FMT);
		String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		if ((contentFormat != null && contentFormat.equals("Unmatched")) || (contentMaterialType != null && contentMaterialType.equals("TM"))) 
		{
			try {
				//Add to purge candidate list with expiry date of 30 days
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.PURGE_CANDIDATE_LIST);
				
				AttributeMap newTask = taskController.getTask(taskID);
				newTask.putAll(messageAttributes);
				Calendar date = Calendar.getInstance();
				date.add(Calendar.DAY_OF_MONTH, 30);
				newTask.setAttribute(Attribute.OP_DATE, date.getTime());
				taskController.saveTask(newTask);
				
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
