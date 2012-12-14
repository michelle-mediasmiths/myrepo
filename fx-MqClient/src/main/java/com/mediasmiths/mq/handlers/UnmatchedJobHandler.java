package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobSubType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class UnmatchedJobHandler implements JobHandler
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
	public UnmatchedJobHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(Job jobMessage)
	{	
		String assetId = jobMessage.getAssetId();
		JobSubType jobSubType = jobMessage.getJobSubType();
		
		log.trace(String.format("assetId %s jobSubType %s", assetId, jobSubType.toString()));
		
		try {
			AttributeMap task = taskController.getTaskForAssetByAssetID(MayamTaskListType.UNMATCHED_MEDIA, assetId);
			
			if (task != null)
			{
				// Populate the Ingest Type (OP_TYPE) field in the unmatched tasklist
				task.setAttribute(Attribute.OP_TYPE, jobSubType.toString());
				taskController.saveTask(task);
			}
			else {
				log.warn("Unable to find Unmatched task for assetId : " + assetId);	
			}
		} catch (MayamClientException e) {
			log.error("Mayam exception thrown while retrieving Unmatched task for asset : " + assetId, e);
		}
	}

	@Override
	public String getName() {
		return "Unmatched Job";
	}

}
