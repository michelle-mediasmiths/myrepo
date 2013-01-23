package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateTxHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(InitiateTxHandler.class);

	@Inject
	private MuleWorkflowController mule;
	
	@Override
	public String getName()
	{
		return "Initiate TX Delivery";
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);
		mule.initiateTxDeliveryWorkflow(houseID, taskID);		
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.TX_DELIVERY;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	
}
