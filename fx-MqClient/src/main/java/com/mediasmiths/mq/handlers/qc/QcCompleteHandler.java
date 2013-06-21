package com.mediasmiths.mq.handlers.qc;


import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class QcCompleteHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(QcCompleteHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);

		String parentAssetID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);

		boolean noParent = false; // assume that items have a parent
		if (parentAssetID == null)
		{
			log.debug("Asset has not parent asset");
			noParent = true; // item does not have a parent, might be unmatched
		}
		
		String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		if (noParent)
		{
			unmatched(houseID);
		}
		else if (MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentMaterialType))
		{
			if (!AssetProperties.isQCParallel(messageAttributes))
			{
				log.info("Asset has not been marked as qc parallel, creating preview task");
				preview(houseID);
			}
			else
			{
				log.info("Asset has been marked as qc parallel, a preview task should already have been created");
			}
		}
		else
		{
			log.info("Asset was not programme or unmatched material");
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
