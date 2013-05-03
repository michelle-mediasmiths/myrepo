package com.mediasmiths.mq.handlers.purge;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class PurgeCandidateUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(PurgeCandidateUpdateHandler.class);

	@Inject
	@Named("purge.presentation.flag.removed.days.default")
	private int defaultPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.editclips")
	private int editClipsPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.associated")
	private int associatedPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.publicity")
	private int publicityPurgeTime;

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
				else if (presentationFlag != null && presentationFlag.equals(Boolean.FALSE))
				{
					//not sure if this will ever actually happen as a presentation item should have been on the 
					//purge candidate list anyway
					
					int numberOfDays = defaultPurgeTime;
					String contentType = currentAttributes.getAttribute(Attribute.CONT_CATEGORY);
					if (contentType != null)
					{
						if (contentType.equals(MayamContentTypes.EPK))
						{
							numberOfDays = associatedPurgeTime;
						}
						else if (contentType.equals(MayamContentTypes.EDIT_CLIPS))
						{
							numberOfDays = editClipsPurgeTime;
						}
						else if (contentType.equals(MayamContentTypes.PUBLICITY))
						{
							numberOfDays = publicityPurgeTime;
						}
					}

					Calendar date = Calendar.getInstance();
					date.add(Calendar.DAY_OF_MONTH, numberOfDays);
					AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
					updateMap.setAttribute(Attribute.OP_DATE, date.getTime());
					updateMap.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
					taskController.saveTask(updateMap);
				}
			}
			catch (MayamClientException e)
			{
				log.error("Exception thrown handling change in presentation flag for material : " + materialID, e);
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
