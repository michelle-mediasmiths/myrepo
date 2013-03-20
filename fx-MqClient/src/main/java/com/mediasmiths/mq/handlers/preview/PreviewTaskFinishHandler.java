package com.mediasmiths.mq.handlers.preview;

import java.util.Date;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class PreviewTaskFinishHandler extends TaskStateChangeHandler
{

	private final static Logger log = Logger.getLogger(PreviewTaskFinishHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try
		{
			onPreviewFinished(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Preview Task Finish : ", e);
		}
	}
	
	private void onPreviewFinished(AttributeMap messageAttributes) throws MayamClientException, RemoteException
	{
		String previewResult = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);

		if (previewResult == null)
		{
			log.error("null QC_PREVIEW_RESULT after preview task completed");
		}
		else
		{
			log.info("preview state is " + previewResult);

			boolean passed = false;
			boolean reorder = false;
			boolean fixeditrequired = false;
			boolean stitcheditrequired = false;

			if (previewResult.equals(MayamPreviewResults.PREVIEW_NOT_DONE))
			{
				// preview not done, dont create any new tasks
			}
			else if (previewResult.equals(MayamPreviewResults.PREVIEW_PASSED))
			{
				passed = true;
			}
			else if (previewResult.equals(MayamPreviewResults.PREVIEW_PASSED_BUT_REORDER))
			{
				passed = true;
				reorder = true;
			}
			else if (previewResult.equals(MayamPreviewResults.FIX_EDIT_REQUIRED))
			{
				fixeditrequired = true;
			}
			else if (previewResult.equals(MayamPreviewResults.FIX_EDIT_AND_REORDER))
			{
				fixeditrequired = true;
				reorder = true;
			}
			else if (previewResult.equals(MayamPreviewResults.STITCH_EDIT_REQUIRED))
			{
				stitcheditrequired = true;
			}
			else if (previewResult.equals(MayamPreviewResults.STITCH_EDIT_AND_REORDER))
			{
				stitcheditrequired = true;
				reorder = true;
			}
			else if (previewResult.equals(MayamPreviewResults.PREVIEW_FAIL))
			{
				// preview failed, dont create any new tasks
			}

			if (passed)
			{
				String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				createSegmentationTask(assetID, assetType);
			}

			if (fixeditrequired || stitcheditrequired)
			{
				String materialID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				Date requiredByDate = messageAttributes.getAttribute(Attribute.COMPLETE_BY_DATE);
				createFixStitchTask(materialID, requiredByDate);
			}

			if (passed && reorder)
			{
				String assetId = messageAttributes.getAttribute(Attribute.ASSET_ID);
				PreviewFailed pf = new PreviewFailed();
				pf.setDate((new Date()).toString());
				pf.setTitle(messageAttributes.getAttribute(Attribute.ASSET_TITLE).toString());
				pf.setAssetId(assetId);
				
				eventsService.saveEvent("http://www.foxtel.com.au/ip/preview", "PreviewPassedReorder", pf);
			}
		}

	}
	
	private void createFixStitchTask(String materialID, Date requiredByDate) throws MayamClientException, RemoteException
	{		
		taskController.createFixStictchTaskForMaterial(materialID, requiredByDate);
	}

	private void createSegmentationTask(String assetID, AssetType assetType) throws MayamClientException, RemoteException
	{
		final SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
		System.out.println(String.format("%d segment lists for asset %s", lists.size(), assetID));
		if (lists != null) 
		{
			for (SegmentList segmentList : lists)
			{
				String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
				taskController.createTask(houseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Preview task";
	}



	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PREVIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
