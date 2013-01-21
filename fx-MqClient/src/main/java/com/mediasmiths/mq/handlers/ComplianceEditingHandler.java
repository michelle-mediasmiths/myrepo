package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;

public class ComplianceEditingHandler  extends AttributeHandler{
	
	private final static Logger log = Logger.getLogger(ComplianceEditingHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.COMPLIANCE_EDIT.getText())) 
		{
			try {
				TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
				if (taskState == TaskState.FINISHED) 
				{
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
					AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
							
					SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
					if (lists != null) 
					{
						for (SegmentList segmentList : lists)
						{
							String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
							long taskID = taskController.createTask(houseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
							AttributeMap newTask = taskController.getTask(taskID);
							newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							newTask.setAttribute(Attribute.QC_STATUS, QcStatus.PASS);	
							newTask.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_PASSED);
							taskController.saveTask(newTask);
						}
					}
				}
			}
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling Fix and Stitch Task Message : ", e);
			}
		}
	}
	

	@Override
	public String getName()
	{
		return "Compliance Editing";
	}
}
