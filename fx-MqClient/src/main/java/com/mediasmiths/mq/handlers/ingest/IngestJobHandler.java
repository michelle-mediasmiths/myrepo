package com.mediasmiths.mq.handlers.ingest;

import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.events.IPEvent;
import com.mediasmiths.foxtel.ip.common.events.ObjectFactory;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.foxtel.ip.common.events.report.CreationComplete;

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
		
		if(assetId!=null){
				try {
				AttributeMap task = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.INGEST, assetId);
				
				if (task != null)
				{
					
					TaskState taskState = (TaskState) task.getAttribute(Attribute.TASK_STATE);
					
					if(taskState != TaskState.FINISHED){
					
						AttributeMap updateMap = taskController.updateMapForTask(task);
						
						if (jobStatus.equals(JobStatus.STARTED))
						{
							log.info(String.format("Import started for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
							updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
							taskController.saveTask(updateMap);
						}
						else if (jobStatus.equals(JobStatus.FAILED))
						{
							log.info(String.format("Import failed for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
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
						else if (jobStatus.equals(JobStatus.FINISHED))
						{
							log.info(String.format("Import finished for asset %s (%s)",task.getAttributeAsString(Attribute.HOUSE_ID),assetId));
							updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
							updateMap.setAttribute(Attribute.INGEST_NOTES, ""); //clear ingest notes from any previous failure
							taskController.saveTask(updateMap);
						}
						else {
							log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Ingest task on asset id : " + assetId);
						}
					}
					else{
						log.info("Items ingest task was already finished, I am making no updates");
					}
				}
				else {
					log.warn("Unable to find Ingest task for assetId : " + assetId);	
				}
			} catch (MayamClientException e) {
				log.error("Mayam exception thrown while retrieving Ingest task for asset : " + assetId, e);
			}
		}
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
