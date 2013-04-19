package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;

public class TransferJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(TransferJobHandler.class);
	
	public void process(Job jobMessage)
	{	
		String assetId = jobMessage.getAssetId();
		JobStatus jobStatus = jobMessage.getJobStatus();
		
		log.trace(String.format("assetId %s jobStatus %s", assetId, jobStatus.toString()));
		
		if(assetId!=null)
		{
			AttributeMap task = null;
			long taskId = -1;
			try {
				task = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.TX_DELIVERY, assetId);
				taskId = task.getAttribute(Attribute.TASK_ID);
			} catch (MayamClientException e) {
				log.info("Mayam exception thrown while retrieving Tx Delivery task for asset : " + assetId, e);
			}
			
			try {
				if (task != null)
				{
					log.info("Tx delivery task found for assetId : " + assetId);
					TaskState taskState = (TaskState) task.getAttribute(Attribute.TASK_STATE);
					
					if(taskState == TaskState.SYS_WAIT){
					
						if (jobStatus.equals(JobStatus.STARTED))
						{
							log.info("High Res Transfer job has started for assetId : " + assetId);
						}
						else if (jobStatus.equals(JobStatus.FAILED))
						{
							log.warn("High Res Transfer job has failed for assetId : " + assetId);
							taskController.setTaskToErrorWithMessage(taskId, "High Res Transfer failed");
						}
						else if (jobStatus.equals(JobStatus.FINISHED))
						{
							log.info("High Res Transfer job has completed for assetId : " + assetId);
							task.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							taskController.saveTask(task);
						}
						else {
							log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Transfer task on asset id : " + assetId);
						}
					}
					else{
						log.info("Items tx delivery task is not in a SYS_WAIT state, I am making no updates");
					}
				}
				else {
					log.warn("Unable to find tx delivery task for assetId : " + assetId);
				}
			}
			catch (MayamClientException e) {
				log.warn("Mayam exception thrown while updating Tx Delivery task for asset : " + assetId, e);
			}
		}
	}

	@Override
	public String getName() {
		return "Transfer Job";
	}

}
