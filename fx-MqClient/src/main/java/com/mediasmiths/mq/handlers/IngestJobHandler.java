package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class IngestJobHandler extends JobHandler
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
	public IngestJobHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(Job jobMessage)
	{	
		String assetId = jobMessage.getAssetId();
		JobStatus jobStatus = jobMessage.getJobStatus();
		
		log.trace(String.format("assetId %s jobStatus %s", assetId, jobStatus.toString()));
		
		try {
			AttributeMap task = taskController.getTaskForAssetByAssetID(MayamTaskListType.INGEST, assetId);
			
			if (task != null)
			{
				if (jobStatus.equals(JobStatus.STARTED))
				{
					task.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(task);
				}
				else if (jobStatus.equals(JobStatus.FAILED))
				{
					task.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
					taskController.saveTask(task);	
				}
				else if (jobStatus.equals(JobStatus.FINISHED))
				{
					task.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
					taskController.saveTask(task);
				}
				else {
					log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Ingest task on asset id : " + assetId);
				}
			}
			else {
				log.warn("Unable to find Ingest task for assetId : " + assetId);	
			}
		} catch (MayamClientException e) {
			log.error("Mayam exception thrown while retrieving Ingest task for asset : " + assetId, e);
		}
	}

	@Override
	public String getName() {
		return "Ingest Job";
	}

}
