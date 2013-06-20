package com.mediasmiths.mq.handlers.unmatched;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

public class UnmatchedTaskCreateHandler extends TaskStateChangeHandler
{

	private final static Logger log = Logger.getLogger(UnmatchedTaskCreateHandler.class);

	@Inject
	@Named("purge.associated.material.without.title.days")
	private int purgeTimeForAssociatedMaterialWithoutTitle;


	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{

		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		String contMatType = (String) messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);

		if (contMatType != null && contMatType.equals(MayamMaterialController.ASSOCIATED_MATERIAL_CONTENT_TYPE))
		{
			log.info("unmatched task created for associated content, adding to purge candidate list");
			try
			{
				taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.MATERIAL,
				                                                        messageAttributes.getAttributeAsString(Attribute.ASSET_SITE_ID),
				                                                        purgeTimeForAssociatedMaterialWithoutTitle);
			}
			catch (MayamClientException e)
			{
				log.error("error adding asset to purge candidate list", e);
			}
		}
	}


	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}


	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}


	@Override
	public String getName()
	{
		return "Unmatched Task Create";
	}
}
