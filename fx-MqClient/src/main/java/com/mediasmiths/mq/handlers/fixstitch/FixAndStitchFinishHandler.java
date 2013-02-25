package com.mediasmiths.mq.handlers.fixstitch;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class FixAndStitchFinishHandler  extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(FixAndStitchFinishHandler.class);
	
	@Override
	public String getName()
	{
		return "Fix and Stitch finish";
		
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try{
			String assetID = (String) messageAttributes.getAttribute(Attribute.ASSET_ID);
			String materialID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
			AssetType assetType = (AssetType) messageAttributes.getAttribute(Attribute.ASSET_TYPE);
			
			AttributeMap assetAttributes = tasksClient.assetApi().getAsset(assetType, assetId);
			assetAttributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_PASSED);
			tasksClient.assetApi().updateAsset(assetAttributes);
			
			final SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
			if (lists != null) 
			{
				log.info(String.format("%d segment lists found for material %s", lists.size(), materialID));
				
				for (SegmentList segmentList : lists)
				{
					String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
					long taskID = taskController.createTask(houseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
				}
			}
			
		}
		catch(Exception e){
			log.error("error handling fix and stich task finish",e);
		}
	}

	
	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.FIX_STITCH_EDIT;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
