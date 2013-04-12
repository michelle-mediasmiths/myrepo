package com.mediasmiths.mq.handlers.ingest;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobSubType;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.events.report.CreationComplete;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class IngestJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);
	
	@Inject
	@Named("fxcommon.serialiser")
	JAXBSerialiser fxcommonSerialiser;
	
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
			else {
				log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Ingest task on asset id : " + assetId);
			}
		}
		else{
			log.info("Items ingest task was already finished, I am making no updates");
		}
	}
	
	private void itemHasNoIngestTask(Job jobMessage, String assetId, JobStatus jobStatus)
			throws MayamClientException{
	
		if ( ! jobStatus.equals(JobStatus.FINISHED)){ //if there is no ingest task we not interested in jobs that have not finished
			log.info("Job has not finished and there is no ingest task, no action taken");
			return;
		}
		
		AttributeMap material;
		try
		{
			material = materialController.getMaterialByAssetId(assetId);
		}
		catch (MayamClientException e1)
		{
			log.error("error fetching asset "+ assetId,e1);
			return;
		}

		String contentMaterialType = material.getAttribute(Attribute.CONT_MAT_TYPE);
		
		if(contentMaterialType == null){
			log.warn("No content type set on asset "+assetId);
		}
		else if (contentMaterialType.equals(MayamContentTypes.UNMATCHED)) //check if content type is unmatched
		{
			log.info("An ingest job has finished for some unmatched content");
		}
		
	}
	

	private void itemHasIngestTaskJobFinished(Job jobMessage, String assetId, AttributeMap task) throws MayamClientException
	{
		log.info(String.format("Import finished for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		JobSubType jobSubType = jobMessage.getJobSubType();
		if (null == jobSubType || (!jobSubType.equals(JobSubType.DART)))
		{
			AttributeMap updateMap = taskController.updateMapForTask(task);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
			updateMap.setAttribute(Attribute.INGEST_NOTES, ""); //clear ingest notes from any previous failure
			taskController.saveTask(updateMap);
		}
		
		try {
			GregorianCalendar c = new GregorianCalendar();
			Date dateUpdated = jobMessage.getJobUpdated();
			if(null == dateUpdated)
			{
				log.debug("Job message had no jobUpdated date set; setting it to now.");
				dateUpdated = new Date();
			}
			c.setTime(dateUpdated);
			XMLGregorianCalendar eventUpdateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			sendImportCompleteEvent(task.getAttributeAsString(Attribute.HOUSE_ID), eventUpdateTime);
		}
		catch(DatatypeConfigurationException e)
		{
			log.error("Datatype Configuration exception thrown while converting update time for %s : " + assetId, e);
		}
	}

	private void itemHasIngestTaskJobFailed(Job jobMessage, String assetId, AttributeMap task)
	{
		log.info(String.format("Import failed for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		AttributeMap updateMap = taskController.updateMapForTask(task);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);

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
		
		sendImportFailureEvent(assetId, jobID);
	}

	private void itemHasIngestTaskJobStarted(String assetId, AttributeMap task) throws MayamClientException
	{
		log.info(String.format("Import started for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
		AttributeMap updateMap = taskController.updateMapForTask(task);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
		taskController.saveTask(updateMap);
	}

	private void sendImportFailureEvent(String assetId, String jobID)
	{
		ArdomeJobFailure ajf = new ArdomeJobFailure();
		ajf.setAssetID(assetId);
		ajf.setJobID(jobID);
		
		String event = fxcommonSerialiser.serialise(ajf);
		String eventName = "ArdomeImportFailure";
		String namespace = contentEventNamespace;
		
		eventsService.saveEvent(eventName, event, namespace);
	}
	
	private void sendImportCompleteEvent(String mediaId, XMLGregorianCalendar completionDate)
	{
		CreationComplete cce = new CreationComplete();
		cce.setMediaID(mediaId);
		cce.setCompletionDate(completionDate);
		
		String event = fxcommonSerialiser.serialise(cce);
		String eventName = "CreationComplete";
		String namespace = bmsEventsNamespace;
		
		eventsService.saveEvent(eventName, event, namespace);
	}

	@Override
	public String getName() {
		return "Ingest Job";
	}

}
