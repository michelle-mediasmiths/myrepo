package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class PreviewTaskHandler implements AttributeHandler
{
	private static final String PREVIEW_FAIL = "fail";
	private static final String STITCH_EDIT_AND_REORDER = "stitchr";
	private static final String STITCH_EDIT_REQUIRED = "stitch";
	private static final String FIX_EDIT_AND_REORDER = "fixr";
	private static final String PREVIEW_PASSED_BUT_REORDER = "passr";
	private static final String PREVIEW_PASSED = "pass";
	private static final String PREVIEW_NOT_DONE = "pvnd";
	private static final String FIX_EDIT_REQUIRED = "fix";
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(PreviewTaskHandler.class);

	public PreviewTaskHandler(MayamTaskController controller)
	{
		taskController = controller;
	}

	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.PREVIEW.getText()))
		{
			try
			{
				TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
				if (taskState == TaskState.FINISHED_FAILED)
				{
					onPreviewFailure(messageAttributes);
				}
				else if (taskState == TaskState.FINISHED)
				{
					onPreviewFinished(messageAttributes);
				}
			}
			catch (Exception e)
			{
				log.error("Exception in the Mayam client while handling Preview Task Message : ",e);
			}
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
			log.info("preview result is " + previewResult);

			boolean passed = false;
			boolean reorder = false;
			boolean fixeditrequired = false;
			boolean stitcheditrequired = false;

			if (previewResult.equals(PREVIEW_NOT_DONE))
			{
				// preview not done, dont create any new tasks
			}
			else if (previewResult.equals(PREVIEW_PASSED))
			{
				passed = true;
			}
			else if (previewResult.equals(PREVIEW_PASSED_BUT_REORDER))
			{
				passed = true;
				reorder = true;
			}
			else if (previewResult.equals(FIX_EDIT_REQUIRED))
			{
				fixeditrequired = true;
			}
			else if (previewResult.equals(FIX_EDIT_AND_REORDER))
			{
				fixeditrequired = true;
				reorder = true;
			}
			else if (previewResult.equals(STITCH_EDIT_REQUIRED))
			{
				stitcheditrequired = true;
			}
			else if (previewResult.equals(STITCH_EDIT_AND_REORDER))
			{
				stitcheditrequired = true;
				reorder = true;
			}
			else if (previewResult.equals(PREVIEW_FAIL))
			{
				// preview failed, dont create any new tasks
			}

			if (passed)
			{
				String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				createSegmentationTask(assetID, assetType);
			}

			if (fixeditrequired || stitcheditrequired)
			{
				String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
				createFixStitchTask(assetID, assetType);
			}

			if (reorder)
			{
				// TODO: An email notification is sent to the channel owner advising the content needs to be reordered. The fault comments entered by the user are included in the message body
			}

		}

	}

	private void createFixStitchTask(String assetID, AssetType assetType) throws MayamClientException, RemoteException
	{
		taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.FIX_STITCH_EDIT);
	}

	private void createSegmentationTask(String assetID, AssetType assetType) throws MayamClientException, RemoteException
	{
		taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.SEGMENTATION);
	}

	private void onPreviewFailure(AttributeMap messageAttributes) throws MayamClientException
	{

		// update the items preview status to fail
		String assetId = messageAttributes.getAttribute(Attribute.ASSET_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		AttributeMap updateAssetMap = taskController.getTasksClient().createAttributeMap();
		updateAssetMap.setAttribute(Attribute.ASSET_ID, assetId);
		updateAssetMap.setAttribute(Attribute.ASSET_TYPE, type);
		updateAssetMap.setAttribute(Attribute.QC_PREVIEW_RESULT, PREVIEW_FAIL);
		try
		{
			taskController.getTasksClient().assetApi().updateAsset(updateAssetMap);
		}
		catch (RemoteException e)
		{
			log.error("Error updating assets preview status", e);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, e);
		}
		finally
		{
			// TODO: An email notification is sent to the relevant channel owner alerting them that the content should be reordered.
		}
	}

	@Override
	public String getName()
	{
		return "Preview task";
	}
}
