package com.mediasmiths.mq.handlers.preview;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

import java.util.Date;

public class PreviewTaskFailHandler extends TaskStateChangeHandler
{

	private final static Logger log = Logger.getLogger(PreviewTaskFailHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try
		{
			onPreviewFailure(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Preview Task failure : ", e);
		}
	}

	private void onPreviewFailure(AttributeMap messageAttributes) throws MayamClientException
	{

		// update the items preview status to fail
		String assetId = messageAttributes.getAttribute(Attribute.ASSET_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		AttributeMap updateAssetMap = taskController.updateMapForAsset(messageAttributes);
		updateAssetMap.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_FAIL);
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
			PreviewFailed pf = new PreviewFailed();
			pf.setDate((new Date()).toString());
			pf.setTitle(messageAttributes.getAttribute(Attribute.ASSET_TITLE).toString());
			pf.setAssetId(assetId);

			eventsService.saveEvent("http://www.foxtel.com.au/ip/qc", "PreviewFailed", pf);

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
		return TaskState.FINISHED_FAILED;
	}
}
