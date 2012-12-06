package com.mediasmiths.mq.handlers;

import java.util.Map;

import org.apache.log4j.Logger;


import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class IngestJobHandler implements PropertiesHandler
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
	public IngestJobHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(Map<String, String> messageProperties)
	{	
		String assetId = messageProperties.get("assetId");
		String jobStatus = messageProperties.get("jobStatus");
		
		try {
			AttributeMap task = taskController.getTaskForAsset(MayamTaskListType.INGEST, assetId);
			
			if (task != null)
			{
				if (jobStatus.equals("STARTED"))
				{
					task.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(task);
				}
				else if (jobStatus.equals("FAILED"))
				{
					task.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
					taskController.saveTask(task);	
				}
				else if (jobStatus.equals("FINISHED"))
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
