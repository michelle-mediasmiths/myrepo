package com.mediasmiths.mq.handlers.compliance;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

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
		
		String sourceHouseID = messageAttributes.getAttribute(Attribute.SOURCE_HOUSE_ID);
		AttributeMap sourceAsset = materialController.getMaterialAttributes(sourceHouseID);
				
		AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
		updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.PASS);	
		updateMap.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_PASSED);
		updateMap.setAttribute(Attribute.CONT_FMT, sourceAsset.getAttribute(Attribute.CONT_FMT));
		updateMap.setAttribute(Attribute.AUDIO_TRACKS, sourceAsset.getAttribute(Attribute.AUDIO_TRACKS));
		tasksClient.assetApi().updateAsset(updateMap);
		
		SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
		if (lists != null) 
		{
			for (SegmentList segmentList : lists)
			{
				String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
				long taskID = taskController.createSegmentationTaskForPackage(houseID);
				log.info(String.format("Segmentation task for package %s has  id  %s",houseID,taskID));
			}
		}
		
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling compliance editing Task Message : ", e);
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
