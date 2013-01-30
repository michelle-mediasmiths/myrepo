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
		
		try{
			String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			Boolean qcRequired = (Boolean) messageAttributes.getAttribute(Attribute.QC_REQUIRED);
			
			if(qcRequired == null){
				log.warn("Qc requirement null!");
				qcRequired = Boolean.FALSE;
			}
	
			// check classification is set, awaiting a means of seeing if the user wishes to override this requirement
			if (!AssetProperties.isClassificationSet(messageAttributes))
			{
				log.info(String.format("Classification not set on package %s reopening segmentation task", houseID));
				taskController.createErrorTXDeliveryTaskForPackage(houseID, "Classification not set");
			} // check package has required number of segments, awaiting a means of seeing if the user wishes to override this requirement
			else if (!packageController.packageHasRequiredNumberOfSegments(houseID))
			{
				log.info(String.format("Package %s does not have the required number of segments", houseID));
				taskController.createErrorTXDeliveryTaskForPackage(houseID, "Number of segments does not match requirements");
			}
			else
			{
				log.debug("Creating tx delivery task");
				taskController.createTXDeliveryTaskForPackage(houseID, qcRequired.booleanValue());
			}
		}
		catch(Exception e){
			log.error("exception in segmentation complete handler",e);
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
