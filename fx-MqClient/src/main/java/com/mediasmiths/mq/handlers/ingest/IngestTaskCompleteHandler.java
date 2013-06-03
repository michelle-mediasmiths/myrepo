package com.mediasmiths.mq.handlers.ingest;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class IngestTaskCompleteHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(IngestTaskCompleteHandler.class);

	@Override
	public String getName()
	{
		return "Ingest Task Complete";
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		// ingest task finished, open qc task
		try
		{
			String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			String previewStatus = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);

			taskController.createQCTaskForMaterial(assetID, previewStatus, messageAttributes);

			if (AssetProperties.isQCParallel(messageAttributes))
			{
				log.info(String.format("QC Parallel is set for item with house id %s, creating preview task for it", assetID));
				taskController.createPreviewTaskForMaterial(assetID);
			}

		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client when creating qc task: ", e);
		}
		
		//send event for reporting
		sendMaterialOrderStatus(messageAttributes);
	}

	@Inject
	DateUtil dateUtil;
	
	private void sendMaterialOrderStatus(AttributeMap currentAttributes)
	{
		if (AssetProperties.isMaterialProgramme(currentAttributes))
		{
			log.debug("Creating event to record order completion");
			try
			{
				com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial addOrUpdateMaterial = new com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial();

				addOrUpdateMaterial.setAggregatorID((String) currentAttributes.getAttribute(Attribute.AGGREGATOR));
				Date completedDate = currentAttributes.getAttribute(Attribute.TASK_UPDATED);

				if (completedDate != null)
				{
					addOrUpdateMaterial.setCompletionDate(dateUtil.fromDate(completedDate));
				}
				else
				{
					log.warn("task updated date is null!");
				}
				addOrUpdateMaterial.setMaterialID((String) currentAttributes.getAttribute(Attribute.HOUSE_ID));
				addOrUpdateMaterial.setTitleID((String) currentAttributes.getAttribute(Attribute.PARENT_HOUSE_ID));
				addOrUpdateMaterial.setOrderReference((String) currentAttributes.getAttribute(Attribute.REQ_REFERENCE));
				
				addOrUpdateMaterial.setFilesize(currentAttributes.getAttributeAsString(Attribute.MEDIA_FILE_SIZE));
				addOrUpdateMaterial.setTitleLength(currentAttributes.getAttributeAsString(Attribute.ASSET_DURATION));
				
				Date requiredBy = currentAttributes.getAttribute(Attribute.COMPLETE_BY_DATE);
				
				if (requiredBy != null)
				{
					addOrUpdateMaterial.setRequiredBy(dateUtil.fromDate(requiredBy));
				}
				
				String ingestNotes = (String) currentAttributes.getAttribute(Attribute.INGEST_NOTES);
				
				if (MayamMaterialController.UNMATCHED_ASSET_MATCHED_TO_THIS_PLACEHOLDER.equals(ingestNotes))
				{
					addOrUpdateMaterial.setTaskType("UNMATCHED");
				}
				else
				{
					addOrUpdateMaterial.setTaskType("INGEST");
				}

				// send event
				eventsService.saveEvent("http://www.foxtel.com.au/ip/bms", "AddOrUpdateMaterial", addOrUpdateMaterial);
			}
			catch (Throwable e)
			{
				log.error("Unable to send event report.", e);
			}
		}
		else
		{
			log.debug("Finished ingest was not for programme material");
		}
	}
	
	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.INGEST;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
