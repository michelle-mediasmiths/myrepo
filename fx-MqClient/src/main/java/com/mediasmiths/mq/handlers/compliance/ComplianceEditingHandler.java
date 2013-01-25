package com.mediasmiths.mq.handlers.compliance;

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
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class ComplianceEditingHandler  extends TaskStateChangeHandler{
	
	private final static Logger log = Logger.getLogger(ComplianceEditingHandler.class);

	@Override
	public String getName()
	{
		return "Compliance Editing";
	}


	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try{
		String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
		String assetHouseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				
		AttributeMap asset = tasksClient.assetApi().getAsset(assetType, assetID);
		asset.setAttribute(Attribute.QC_STATUS, QcStatus.PASS);	
		asset.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_PASSED);
		tasksClient.assetApi().updateAsset(asset);
		
		taskController.createTask(assetHouseID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.QC_VIEW, asset);
		
		SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
		if (lists != null) 
		{
			for (SegmentList segmentList : lists)
			{
				String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
				long taskID = taskController.createTask(houseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
				AttributeMap newTask = taskController.getTask(taskID);
				newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
				taskController.saveTask(newTask);
			}
		}
		
		closeTask(messageAttributes);
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling Fix and Stitch Task Message : ", e);
		}
	}


	
	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.COMPLIANCE_EDIT;
	}


	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
