package com.mediasmiths.mq.handlers.qc;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class QcCompleteHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(QcCompleteHandler.class);


	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);

		String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		if (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED))
		{
			unmatched(houseID);
		}
		else
		{
			preview(houseID);
		}
		
	}
	
	private void preview(String houseID)
	{
		try
		{

			taskController.createPreviewTaskForMaterial(houseID);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling QC Task Complete Message : ", e);
		}
	}

	private void unmatched(String houseID)
	{
		try
		{
			taskController.createUnmatchedTaskForMaterial(houseID);
		}
		catch (MayamClientException e)
		{
			log.error("error creating unmatched task for asset " + houseID, e);
		}
	}

	@Override
	public String getName()
	{
		return "QC Complete";
	}


	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;		
	}
}
