package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class SegmentationCompleteHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(SegmentationCompleteHandler.class);
	
	@Inject
	private MuleWorkflowController mule;
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.SEGMENTATION.getText())) 
		{
			try {
				TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
				if (taskState == TaskState.FINISHED) 
				{
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
					AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
					long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.TX_DELIVERY);
					AttributeMap newTask = taskController.getTask(taskID);
					newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
					taskController.saveTask(newTask);
					mule.initiateTxDeliveryWorkflow(assetID);	
				}
			}
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling Segmentation Task Complete Message : ", e);
			}
		}	
	}

	@Override
	public String getName()
	{
		return "Segmentation Complete";
	}
}
