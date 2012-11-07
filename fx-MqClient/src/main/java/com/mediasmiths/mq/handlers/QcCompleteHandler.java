package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class QcCompleteHandler 
{
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(QcCompleteHandler.class);
	
	public QcCompleteHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.QC_VIEW)) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.FINISHED) 
			{
				try {
					messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
					taskController.saveTask(messageAttributes);
					
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
					String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
					long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.PREVIEW);
					AttributeMap newTask = taskController.getTask(taskID);
					newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
					taskController.saveTask(newTask);
				}
				catch (Exception e) {
					log.error("Exception in the Mayam client while handling QC Task Complete Message : " + e);
					e.printStackTrace();
				}
			}	
		}
	}
}
