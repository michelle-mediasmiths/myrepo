package com.mediasmiths.mq.handlers.ingest;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;

public class IngestJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
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
					log.info(String.format("Import started for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
					task.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(task);
				}
				else if (jobStatus.equals(JobStatus.FAILED))
				{
					log.info(String.format("Import failed for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
					task.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);

					try
					{
						String jobID = jobMessage.getJobId();
						JobType jobType = jobMessage.getJobType();
						String ingestNotes = String.format("Job %s of type %s failed", jobID, jobType.toString());
						task.setAttribute(Attribute.INGEST_NOTES, ingestNotes);
					}
					catch (Exception e)
					{
						log.error("Error setting failure reason on ingest task", e);
					}
					
					taskController.saveTask(task);	
				}
				else if (jobStatus.equals(JobStatus.FINISHED))
				{
					log.info(String.format("Import finished for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
					task.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
					task.setAttribute(Attribute.INGEST_NOTES, ""); //clear ingest notes from any previous failure
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