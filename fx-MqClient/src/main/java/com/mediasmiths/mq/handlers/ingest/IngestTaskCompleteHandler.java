package com.mediasmiths.mq.handlers.ingest;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Source;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import com.sun.xml.bind.Util;

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

				addOrUpdateMaterial.setAggregatorID(currentAttributes.getAttributeAsString(Attribute.AGGREGATOR));
				Date completedDate = currentAttributes.getAttribute(Attribute.TASK_UPDATED);
				addOrUpdateMaterial.setCompletionDate(dateUtil.fromDate(completedDate));
				addOrUpdateMaterial.setMaterialID(currentAttributes.getAttributeAsString(Attribute.HOUSE_ID));

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
