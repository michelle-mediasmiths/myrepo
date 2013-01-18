package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

public class QcCompleteHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(QcCompleteHandler.class);

	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.QC_VIEW.getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
			if (taskState == TaskState.FINISHED)
			{
				String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);

				String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
				if (contentMaterialType != null && contentMaterialType.equals("TM"))
				{
					unmatched(houseID);
				}
				else{
					preview(houseID);
				}
			}
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
}
