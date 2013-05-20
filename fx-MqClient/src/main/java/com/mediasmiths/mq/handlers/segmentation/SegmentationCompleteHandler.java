package com.mediasmiths.mq.handlers.segmentation;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class SegmentationCompleteHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(SegmentationCompleteHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{

		log.info("segmentation complete");

		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		String parentHouseID = messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);

		log.info(String.format("segmentation complelete houseid = %s parentHouseID = %s", houseID, parentHouseID));
		
		try
		{
			AttributeMap material = materialController.getMaterialAttributes(parentHouseID);

			if (AssetProperties.isAO(material))
			{
				log.info("material is ao, marking package as tx ready");
				AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
				updateMap.setAttribute(Attribute.TX_READY, Boolean.TRUE);
				tasksClient.assetApi().updateAsset(updateMap);
			}
			else
			{
				log.info("material is not ao");
				Boolean qcRequired = (Boolean) messageAttributes.getAttribute(Attribute.QC_REQUIRED);

				if (qcRequired == null)
				{
					log.warn("Qc requirement null!");
					qcRequired = Boolean.FALSE;
				}

				long taskId = -1;
				
				// check classification is set, awaiting a means of seeing if the user wishes to override this requirement
				if (!AssetProperties.isClassificationSet(messageAttributes))
				{
					log.info(String.format("Classification not set on package %s reopening segmentation task", houseID));
					taskId = taskController.createErrorTXDeliveryTaskForPackage(houseID, "Classification not set");
				}
				else
				{
					log.debug("Creating tx delivery task");
					taskId = taskController.createTXDeliveryTaskForPackage(houseID, qcRequired.booleanValue());
				}
				
				String username = messageAttributes.getAttributeAsString(Attribute.TASK_UPDATED_BY);
				if (username != null)
				{
					AttributeMap task = taskController.getTask(taskId);
					AttributeMap taskUpdate = taskController.updateMapForTask(task);
					taskUpdate.setAttribute(Attribute.TASK_CREATED_BY, username);
					tasksClient.taskApi().updateTask(taskUpdate);
				}
			}
		}
		catch (Exception e)
		{
			log.error("exception in segmentation complete handler", e);
			
			String title = messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
			String taskID = messageAttributes.getAttributeAsString(Attribute.TASK_ID);
			TcNotification tx = new TcNotification();
			tx.setPackageID(houseID);
			tx.setAssetID(parentHouseID);
			tx.setTitle(title);
			tx.setTaskID(taskID);
						
			// send email regarding failed to send to TX
			eventsService.saveEvent("http://www.foxtel.com.au/ip/tc", "FailedInSendtoTX", tx);
		}
	}

	@Override
	public String getName()
	{
		return "Segmentation Complete";
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.SEGMENTATION;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
