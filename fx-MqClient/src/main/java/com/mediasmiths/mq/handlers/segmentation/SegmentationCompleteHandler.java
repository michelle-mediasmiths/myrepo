package com.mediasmiths.mq.handlers.segmentation;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
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

		try
		{
			log.info("segmentation complete");

			String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			String parentHouseID = messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);

			log.info(String.format("segmentation complelete houseid = %s parentHouseID = %s", houseID, parentHouseID));

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

				// check classification is set, awaiting a means of seeing if the user wishes to override this requirement
				if (!AssetProperties.isClassificationSet(messageAttributes))
				{
					log.info(String.format("Classification not set on package %s reopening segmentation task", houseID));
					taskController.createErrorTXDeliveryTaskForPackage(houseID, "Classification not set");
				}
				else
				{
					log.debug("Creating tx delivery task");
					taskController.createTXDeliveryTaskForPackage(houseID, qcRequired.booleanValue());
				}
			}
		}
		catch (Exception e)
		{
			log.error("exception in segmentation complete handler", e);
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
