package com.mediasmiths.mq.handlers.qc;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class InitiateQcHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(InitiateQcHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		MediaStatus mediaStatus = messageAttributes.getAttribute(Attribute.MEDST_HR);

		if (mediaStatus.equals(MediaStatus.MISSING))
		{
			log.info(String.format(
					"A qc task was created for an item %s with no media, cancelling",
					messageAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
			cancelTask(messageAttributes, "No media attatched to item");
		}
		else
		{
			if (AssetProperties.isQCPassed(messageAttributes))
			{
				log.info(String.format(
						"A qc task was created for an item %s but it has already passed qc, cancelling",
						messageAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
				cancelTask(messageAttributes, "Item has already passed qc");
			}
			else if (MayamPreviewResults.isPreviewPass((String)messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT)))
			{
				log.info(String.format(
						"A qc task was created for an item %s but it has already passed preview, cancelling",
						messageAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
				cancelTask(messageAttributes, "Item has already passed preview");
			}
			else
			{
				fileformatVerification(messageAttributes);
			}
		}
	}

	private void fileformatVerification(AttributeMap messageAttributes)
	{
		// task just created, lets perform file format verification
		try
		{
			materialController.verifyFileMaterialFileFormat(messageAttributes);
		}
		catch (MayamClientException e)
		{
			log.error(
					"MayamClient exception while performing file format verification for asset"
							+ messageAttributes.getAttributeAsString(Attribute.ASSET_ID),
					e);

			String errorMessage = "Error performing file format verification";
			messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
			messageAttributes.setAttribute(Attribute.ERROR_MSG, errorMessage);
			try
			{
				taskController.saveTask(messageAttributes);
			}
			catch (MayamClientException e1)
			{
				log.error("error setting task to error state", e1);
			}
		}
	}

	private void cancelTask(AttributeMap messageAttributes, String errorMessage)
	{
		
		AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
		// a qc task has been created for an item it shouldnt have been, cancel
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REJECTED);
		updateMap.setAttribute(Attribute.ERROR_MSG, errorMessage);
		try
		{
			taskController.saveTask(updateMap);
		}
		catch (MayamClientException e)
		{
			log.error("error cancelling task", e);
		}
	}

	@Override
	public String getName()
	{
		return "Initiate QC";
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}
}
