package com.mediasmiths.mq.handlers.fixstitch;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class FixAndStitchRevertHandler  extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(FixAndStitchRevertHandler.class);
	
	@Override
	public String getName()
	{
		return "Fix and Stitch revert";
		
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		try
		{
			taskController.createPreviewTaskForMaterial(houseID);
		}
		catch (MayamClientException e)
		{
			log.error("Error creating preview task after fix + stich task marked for revert asset: "+houseID,e);
		}
	
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.FIX_STITCH_EDIT;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED_FAILED;
	}
}
