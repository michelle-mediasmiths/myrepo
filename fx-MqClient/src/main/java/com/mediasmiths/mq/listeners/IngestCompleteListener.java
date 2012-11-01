package com.mediasmiths.mq.listeners;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class IngestCompleteListener {
	
	public static Listener getInstance(final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//On compliance editing completion create segmentation tasks
					AttributeMap messageAttributes = msg.getSubject();
					String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
					if (taskListID.equals(MayamTaskListType.INGEST)) 
					{
						TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
						if (taskState == TaskState.FINISHED) 
						{
							messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
							taskController.saveTask(messageAttributes);
								
							String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
							String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							String parentID = "";
							//TODO: Parent ID not yet implemented
							//parentID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
							
							boolean qcRequired = false;
							
							if (qcRequired){
								//TODO: Implement QC condition
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.QC_VIEW);
								AttributeMap newTask = taskController.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								taskController.saveTask(newTask);
							}
							else if (assetType.equals(AssetType.ITEM) && (parentID != null || parentID.equals(""))) 
							{
								//Skip QC and move straight to compliance	
								//Compile flag is not stored in Mayam, but Parent Id is taken from compiled object
								//As such we are using the presence of Parent Id on a Material asset to determined if compile was set
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.COMPLIANCE_LOGGING);
								AttributeMap newTask = taskController.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								taskController.saveTask(newTask);
							}
							else {
								//Skip both compliance and QC flows and go straight to segmentation
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.SEGMENTATION);
								AttributeMap newTask = taskController.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								taskController.saveTask(newTask);
							}
							
						}	
					}
				}
			}
		};
	}
}
