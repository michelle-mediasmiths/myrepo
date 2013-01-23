package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;

public class SegmentationCompleteHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(SegmentationCompleteHandler.class);

	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.SEGMENTATION.getText()))
		{
			try
			{
				TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
				if (taskState == TaskState.FINISHED)
				{
					String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);

					// check classification is set, awaiting a means of seeing if the user wishes to override this requirement
					if (!AssetProperties.isClassificationSet(messageAttributes))
					{
						log.info(String.format("Classification not set on package %s reopening segmentation task", houseID));
						setToWarning(messageAttributes, "Classification not set");
					} // check package has required number of segments, awaiting a means of seeing if the user wishes to override this requirement
					else if (!packageController.packageHasRequiredNumberOfSegments(houseID))
					{
						log.info(String.format("Package %s does not have the required number of segments", houseID));
						setToWarning(messageAttributes, "Number of segments does not match requirements");
					}
					else
					{
						// create tx delivery task
						try
						{
							log.debug("Creating tx delivery task, assuming qc is required");
							taskController.createTXDeliveryTaskForPackage(houseID, true);
						}
						catch (MayamClientException e)
						{
							log.error("error creating tx delivery task for pacakge " + houseID, e);
						}
					}
				}
			}
			catch (Exception e)
			{
				log.error("Exception in the Mayam client while handling Segmentation Task Complete Message : ", e);
			}
		}
	}

	private void setToWarning(AttributeMap messageAttributes, String errorMessage)
	{

		messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
		messageAttributes.setAttribute(Attribute.ERROR_MSG, errorMessage);
		try
		{
			taskController.saveTask(messageAttributes);
		}
		catch (MayamClientException e)
		{
			log.error("Error setting segmentation task state to warning with error message " + errorMessage, e);
		}
	}

	@Override
	public String getName()
	{
		return "Segmentation Complete";
	}
}
