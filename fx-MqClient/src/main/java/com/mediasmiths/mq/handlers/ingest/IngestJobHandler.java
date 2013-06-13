package com.mediasmiths.mq.handlers.ingest;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.ip.common.events.ArdomeImportFailure;
import com.mediasmiths.foxtel.ip.common.events.CreationComplete;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.JobHandler;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.std.util.jaxb.exception.JAXBRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class IngestJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
	@Inject
	@Named("fxcommon.serialiser")
	private JAXBSerialiser fxcommonSerialiser;
	
	@Inject(optional=false)
	@Named("content.events.namespace")
	private String contentEventNamespace;
	
	@Inject(optional=false)
	@Named("bms.events.namespace")
	private String bmsEventsNamespace;
	
	public void process(Job jobMessage)
	{	
		String assetId = jobMessage.getAssetId();
		JobStatus jobStatus = jobMessage.getJobStatus();
		
		log.trace(String.format("assetId %s jobStatus %s", assetId, jobStatus.toString()));
		
		if(assetId!=null)
		{
			AttributeMap task = null;
			try {
				task = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.INGEST, assetId);
			} catch (MayamClientException e) {
				log.info("Mayam exception thrown while retrieving Ingest task for asset : " + assetId, e);
			}
			
			try {
				if (task != null)
				{
					log.info("ingest task found for assetId : " + assetId);
					itemHasIngestTask(jobMessage, assetId, jobStatus, task);
				}
				else {
					log.info("Unable to find Ingest task for assetId : " + assetId);
					itemHasNoIngestTask(jobMessage, assetId, jobStatus);	
				}
			}
			catch (MayamClientException e) {
				log.warn("Mayam exception thrown while updating Ingest task for asset : " + assetId, e);
			}
		}
	}

	private void itemHasIngestTask(Job jobMessage, String assetId, JobStatus jobStatus, AttributeMap task)
			throws MayamClientException
	{
		TaskState taskState = (TaskState) task.getAttribute(Attribute.TASK_STATE);
		
		if(taskState != TaskState.FINISHED){
		
			if (jobStatus.equals(JobStatus.STARTED))
			{
				itemHasIngestTaskJobStarted(assetId, task);
			}
			else if (jobStatus.equals(JobStatus.FAILED))
			{
				itemHasIngestTaskJobFailed(jobMessage, assetId, task);
			}
			else if (jobStatus.equals(JobStatus.FINISHED))
			{
				itemHasIngestTaskJobFinished(jobMessage, assetId, task);
			}
			else if (jobStatus.equals(JobStatus.ABORTED))
			{
				itemHasIngestTaskJobCancelled(jobMessage, assetId, task);
			}
			else
			{
				log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Ingest task on asset id : " + assetId);
			}
		}
		else{
			log.info("Items ingest task was already finished, I am making no updates");
		}
	}
	
	private void itemHasIngestTaskJobCancelled(Job jobMessage, String assetId, AttributeMap task)
	{
		log.info("Ingest was aborted, setting task back to OPEN state");
		AttributeMap updateMapForTask = taskController.updateMapForTask(task);
		updateMapForTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
		try
		{
			taskController.saveTask(updateMapForTask);
		}
		catch (MayamClientException e)
		{
			log.error("error moving ingest task for asset " + assetId + " to open state", e);
			try
			{
				taskController.createWFEErrorTaskBySiteID(MayamAssetType.MATERIAL,
				                                          task.getAttributeAsString(Attribute.ASSET_SITE_ID),
				                                          "error moving ingest task for asset " + assetId + " to open state");
			}
			catch (MayamClientException e1)
			{
				log.error("error creating error task", e);
				// give up
			}
		}
	}


	private void itemHasNoIngestTask(Job jobMessage, String assetId, JobStatus jobStatus)
	{
		log.debug("Asset has no ingest task");

		Job.JobSubType jobSubType = jobMessage.getJobSubType();

		if (jobStatus.equals(JobStatus.FINISHED))
		{
			if (assetId != null)
			{
				AttributeMap material;
				try
				{
					log.debug("Fetching asset to check if it is unmatched");
					material = materialController.getMaterialByAssetId(assetId);
				}
				catch (MayamClientException e1)
				{
					log.error("error fetching asset " + assetId, e1);
					return;
				}

				String contentMaterialType = material.getAttribute(Attribute.CONT_MAT_TYPE);

				boolean isUnmatched = contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED);
				boolean isUnmatchedDart = false;

				if (jobSubType.equals(Job.JobSubType.DART))
				{
					//logging this purely to see if in the future we need to bother with the isUnmatchedDart
					//as this is from the original 'UnmatchedJobHandler' whose logic has been moved to this class
					//we now know there is not an ingest task at this stage, and as dart items are ingested to placeholders this check may be redundant!
					log.info("Job subtype is dart");
					isUnmatchedDart = true;
				}

				//check content type is unmatched
				if (isUnmatched && (!isUnmatchedDart))
				{
					log.debug("Item is unmatched item, job sub type is not dart");

					//attempt to set the source as import or ingest
					AttributeMap updateMap = taskController.updateMapForAsset(material);
					updateMap.setAttribute(Attribute.OP_TYPE, jobSubType.toString());

					String currenttitle = material.getAttributeAsString(Attribute.ASSET_TITLE);

					if (StringUtils.isEmpty(currenttitle))
					{
						log.debug("Current ASSET_TITLE of unmatched item is empty, populating from SERIES_TITLE");
						try
						{
							String fileName = material.getAttributeAsString(Attribute.SERIES_TITLE);
							log.debug("SERIES_TITLE : " + fileName);
							updateMap.setAttribute(Attribute.ASSET_TITLE, fileName);
						}
						catch (Exception e)
						{
							log.error("error setting asset title on unmatched item", e);
						}
					}

					try
					{
						log.debug("Updating item to set OP_TYPE and optionally ASSET_TITLE");
						tasksClient.assetApi().updateAsset(updateMap);
					}
					catch (RemoteException e)
					{
						log.error("Error setting OP_TYPE on unmatched material", e);
					}

					// create qc task for material if there hasnt been one before
					String houseID = material.getAttributeAsString(Attribute.HOUSE_ID);
					try
					{
						log.debug("Attempting to create qcTask for unmatched item");
						//there is no ingest task for this asset so we create a qc task now that it is ingested
						taskController.createQCTaskForMaterial(houseID, null, material);
					}
					catch (MayamClientException e)
					{
						log.error("Error searching for or creating qc task for asset " + assetId, e);
					}
				}
			}
		}
	}


	private void itemHasIngestTaskJobFinished(Job jobMessage, String assetId, AttributeMap task) throws MayamClientException
	{
		log.info(String.format("Import finished for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		log.debug("Recording that ingest finished has been seen for this asset");
		
		AttributeMap updateMap = taskController.updateMapForTask(task);
		
		updateMap.setAttribute(Attribute.INGEST_NOTES, ""); //clear ingest notes from any previous failure
		updateMap.setAttribute(Attribute.APP_FLAG, Boolean.TRUE); //record that ingest finished event has been seen for this asset
		
		taskController.saveTask(updateMap);
		
		try
		{
			GregorianCalendar c = new GregorianCalendar();
			Date dateUpdated = jobMessage.getJobUpdated();
			if (null == dateUpdated)
			{
				log.debug("Job message had no jobUpdated date set; setting it to now.");
				dateUpdated = new Date();
			}
			c.setTime(dateUpdated);
			XMLGregorianCalendar eventUpdateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			sendImportCompleteEvent(task.getAttributeAsString(Attribute.HOUSE_ID), eventUpdateTime);
		}
		catch (DatatypeConfigurationException e)
		{
			log.error("Datatype Configuration exception thrown while converting update time for %s : " + assetId, e);
		}
		catch (JAXBRuntimeException e)
		{
			log.error("jaxb exception", e);
		}
	}

	private void itemHasIngestTaskJobFailed(Job jobMessage, String assetId, AttributeMap task)
	{
		log.info(String.format("Import failed for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		AttributeMap updateMap = taskController.updateMapForTask(task);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
		updateMap.setAttribute(Attribute.APP_FLAG, Boolean.FALSE); //we got an ingest job event but it wasnt a pass

		String jobID = jobMessage.getJobId();
		JobType jobType = jobMessage.getJobType();
		
		try
		{
			String ingestNotes = String.format("Job %s of type %s failed", jobID, jobType.toString());
			updateMap.setAttribute(Attribute.INGEST_NOTES, ingestNotes);
			taskController.saveTask(updateMap);	
		}
		catch (Exception e)
		{
			log.error("Error setting failure reason on ingest task", e);
		}

		sendImportFailureEvent(assetId, jobID, task.getAttributeAsString(Attribute.HOUSE_ID),(StringList)task.getAttribute(Attribute.CHANNELS), AssetProperties.isAO(task));
	}

	private void itemHasIngestTaskJobStarted(String assetId, AttributeMap task) throws MayamClientException
	{
		log.info(String.format("Import started for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		AttributeMap updateMap = taskController.updateMapForTask(task);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
		taskController.saveTask(updateMap);
	}

	private void sendImportFailureEvent(String assetId, String jobID, String houseID, StringList channels, boolean isAo)
	{
		ArdomeImportFailure ajf = new ArdomeImportFailure();
		ajf.setAssetID(assetId);
		ajf.setJobID(jobID);
		ajf.setFilename(houseID);
		ajf.getChannelGroup().addAll(channelProperties.groupsForEmail(channels,isAo));	
		
		String event = fxcommonSerialiser.serialise(ajf);
		String eventName;

		if (isAo)
		{
			eventName = EventNames.ARDOME_IMPORT_FAILURE_AO;
		}
		else
		{
			eventName = EventNames.ARDOME_IMPORT_FAILURE_NON_AO;
		}

		String namespace = contentEventNamespace;

		eventsService.saveEvent(eventName, event, namespace);
	}
	
	private void sendImportCompleteEvent(String mediaId, XMLGregorianCalendar completionDate)
	{
		CreationComplete cce = new CreationComplete();
		cce.setMediaID(mediaId);
		cce.setCompletionDate(completionDate);
		
		String event = fxcommonSerialiser.serialise(cce);
		String eventName = EventNames.CREATION_COMPLETE;
		String namespace = bmsEventsNamespace;
		
		eventsService.saveEvent(eventName, event, namespace);
	}

	@Override
	public String getName() {
		return "Ingest Job";
	}

}
