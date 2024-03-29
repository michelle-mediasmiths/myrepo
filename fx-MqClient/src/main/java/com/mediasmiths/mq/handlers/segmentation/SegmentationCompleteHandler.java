package com.mediasmiths.mq.handlers.segmentation;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

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
					log.debug("Creating tx delivery task if no other already exists");
					String userWhoFinishedSegmentationTask = messageAttributes.getAttribute(Attribute.TASK_UPDATED_BY);
					taskId = taskController.createTXDeliveryTaskForPackage(houseID,
					                                                       qcRequired.booleanValue(),
					                                                       userWhoFinishedSegmentationTask);
				}
			}
		}
		catch (Exception e)
		{
			log.error("exception in segmentation complete handler", e);
			
			String title = messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
			String taskID = messageAttributes.getAttributeAsString(Attribute.TASK_ID);
			TcEvent tx = new TcEvent();
			tx.setPackageID(houseID);
			tx.setAssetID(parentHouseID);
			tx.setTitle(title);
			tx.setTaskID(taskID);
						
			// send email regarding failed to send to TX
			eventsService.saveEvent("http://www.foxtel.com.au/ip/tc", EventNames.FAILED_IN_SENDTO_TX, tx);
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
