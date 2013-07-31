package com.mediasmiths.mq.handlers.purge;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import org.apache.log4j.Logger;

public class PurgeCandidateUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(PurgeCandidateUpdateHandler.class);

	@Inject
	PurgeEvent purgeEvent;

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{

		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);

		if (!MayamAssetType.MATERIAL.getAssetType().equals(assetType))
		{
			return; // only interested in materials
		}

		if (attributeChanged(Attribute.PRESENTATION_FLAG, before, after, currentAttributes))
		{
			String materialID = currentAttributes.getAttributeAsString(Attribute.HOUSE_ID);

			try
			{
				Boolean presentationFlag = currentAttributes.getAttribute(Attribute.PRESENTATION_FLAG);
				log.info("Presentation flag for " + materialID + "set to " + presentationFlag);

				if (presentationFlag != null && presentationFlag.equals(Boolean.TRUE))
				{
					log.info("Presentation flag for " + materialID + "set, removing purge candidate task");
					AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
					updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
					taskController.saveTask(updateMap);
				}

			}
			catch (MayamClientException e)
			{
				log.error("Exception thrown handling change in presentation flag for material : " + materialID, e);
			}
		}

		if (attributeChanged(Attribute.TASK_STATE, before, after, currentAttributes))
		{
			log.debug("State changed, sending purgeEventNotification");
			purgeEvent.setPurgeEventForPurgeCandidateTask(currentAttributes);
		}


		if (!taskController.taskIsInEndState(currentAttributes)) //if the task is still open
		{
			final QcStatus qcStatus = currentAttributes.getAttribute(Attribute.QC_STATUS);
			if (QcStatus.PASS_MANUAL.equals(qcStatus) || QcStatus.PASS.equals(qcStatus)) //if qc status has been set to pass
			{
				String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
				if (MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentType)) //if programme content
				{
					log.info("A purge candidate entry for programme content with a qc status of pass or pass manual exists, cancelling");
					try
					{
						taskController.cancelTask(currentAttributes);
					}
					catch (MayamClientException e)
					{
						log.error("Error removing purge candidate task",e);
					}
				}
			}
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PURGE_CANDIDATE_LIST;
	}

	@Override
	public String getName()
	{
		return "Purge candidate task update";
	}
}
