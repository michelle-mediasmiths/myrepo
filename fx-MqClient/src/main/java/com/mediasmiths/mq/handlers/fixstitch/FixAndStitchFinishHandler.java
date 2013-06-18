package com.mediasmiths.mq.handlers.fixstitch;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.SegmentUtil;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

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
			
			AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
			updateMap.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_PASSED);

			String presentation = (String) messageAttributes.getAttribute(Attribute.SEGMENTATION_NOTES);
			String newNotes = SegmentUtil.removePackageIDFromSegmentationNotesString(presentation);
			updateMap.setAttribute(Attribute.SEGMENTATION_NOTES,newNotes);

			tasksClient.assetApi().updateAsset(updateMap);
			
			final SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
			if (lists != null) 
			{
				log.info(String.format("%d segment lists found for material %s", lists.size(), materialID));
				
				for (SegmentList segmentList : lists)
				{
					String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
					// create segmentation task
					long taskID = taskController.createSegmentationTaskForPackage(houseID);
					log.info(String.format("Segmentation task for package %s has  id  %s",houseID,taskID));
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
