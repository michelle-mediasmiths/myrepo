package com.mediasmiths.mq.handlers.qc;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class QcTaskUpdateHandler extends TaskUpdateHandler
{

	private static final String QC_FAILED_RE_ORDER = "QcFailedReOrder";
	private static final String MANUAL_QC_PASS = "pass";
	private final static String MANUAL_QC_FAIL_WITH_REINGEST = "reingest";
	private final static String MANUAL_QC_FAIL_WITH_REORDER = "reorder";

	private final static Logger log = Logger.getLogger(QcTaskUpdateHandler.class);

	@Inject
	MuleWorkflowController mule;
	
	@Inject(optional=false)
	@Named("qc.events.namespace")
	private String qcEventNamespace;
	
	@Inject
	@Named("wfe.serialiser")
	private JAXBSerialiser serialiser;
	

	@Override
	public String getName()
	{
		return "QCTaskUpdate";
	}

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		// if update was to change substatus for file format verification
		if (after.containsAttribute(Attribute.QC_SUBSTATUS1))
		{
			fileFormatVerificationStatusChanged(currentAttributes, after);
		}

		// autoqc status changed
		if (after.containsAttribute(Attribute.QC_SUBSTATUS2))
		{
			autoQcStatusChanged(currentAttributes, after);
		}

		// qc state set manually
		if (after.containsAttribute(Attribute.QC_RESULT))
		{
			qcResultSetManually(currentAttributes, after);
		}				
		
	}

	private void qcResultSetManually(AttributeMap currentAttributes, AttributeMap after)
	{
		String result = after.getAttribute(Attribute.QC_RESULT);
		try
		{
			AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
			
			if (result.equals(MANUAL_QC_PASS))
			{
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.PASS_MANUAL);
				taskController.saveTask(updateMap);
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REORDER))
			{
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(updateMap);
				
				sendQcFailedReorderEvent(currentAttributes);
				
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REINGEST))
			{
				// user has requested reingest, fail the qc task
				log.debug("User requested uningest, failing qc task");
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(updateMap);

				if (AssetProperties.isMaterialProgramme(currentAttributes)
						|| AssetProperties.isMaterialAssociated(currentAttributes))
				{
					log.debug("Asset is programme or associated content");
					// uningest the media
					log.debug("User requested uningest, uningesting media");
					materialController.uningest(currentAttributes);
					
				}
			}
		}
		catch (MayamClientException e)
		{
			log.error("error updating task status", e);
		}
	}

	private void sendQcFailedReorderEvent(AttributeMap currentAttributes)
	{
		try
		{
			AutoQCErrorNotification aen = new AutoQCErrorNotification();
			aen.setAssetId(currentAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			aen.setForTXDelivery(false);
			aen.setJobName("");
			Long taskId = (Long) currentAttributes.getAttribute(Attribute.TASK_ID);

			if (taskId != null)
			{
				aen.setTaskID(taskId.longValue());
			}

			aen.setTitle(currentAttributes.getAttributeAsString(Attribute.ASSET_TITLE));

			String eventName = QC_FAILED_RE_ORDER;
			String event = serialiser.serialise(aen);
			String namespace = qcEventNamespace;

			eventsService.saveEvent(eventName, event, namespace);
		}
		catch (Exception e)
		{
			log.error("error sending "+QC_FAILED_RE_ORDER+" event",e);
		}
	}

	private void autoQcStatusChanged(AttributeMap currentAttributes, AttributeMap after)
	{
		QcStatus autoQc = after.getAttribute(Attribute.QC_SUBSTATUS2);

		if (autoQc.equals(QcStatus.PASS) || autoQc.equals(QcStatus.PASS_MANUAL))
		{

			// kick off channel condition monitoring once that has been implemented
			finishWithPass(currentAttributes, autoQc);
		}
		else if (autoQc.equals(QcStatus.FAIL))
		{
			try
			{
				
				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
				taskController.saveTask(updateMap);
			}
			catch (MayamClientException e)
			{
				log.error("error settign qc task to finished state", e);
			}
		}
	}

	private void fileFormatVerificationStatusChanged(AttributeMap currentAttributes, AttributeMap after)
	{
		// file format verification status updated
		QcStatus fileFormat = currentAttributes.getAttribute(Attribute.QC_SUBSTATUS1);

		if (fileFormat.equals(QcStatus.PASS) || fileFormat.equals(QcStatus.PASS_MANUAL))
		{
			boolean autoQcRequired = materialController.isAutoQcRequiredForMaterial(currentAttributes);
//			boolean isAutoQcRunOrRunning = materialController.isAutoQcRunOrRunningForMaterial(currentAttributes);

			if (autoQcRequired)
			{
				initiateAutoQc(currentAttributes);
			}
			else
			{
				// kick off channel condition monitoring once that has been implemented
				finishWithPass(currentAttributes,fileFormat);
			}
		}
		else if (fileFormat.equals(QcStatus.FAIL))
		{
			try
			{
				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
				taskController.saveTask(updateMap);
			}
			catch (MayamClientException e)
			{
				log.error("error settign qc task to warning state", e);
			}
		}
	}

	private void finishWithPass(AttributeMap currentAttributes, QcStatus passStatus)
	{
		try
		{
			AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
			updateMap.setAttribute(Attribute.QC_STATUS, passStatus);
			taskController.saveTask(updateMap);
		}
		catch (MayamClientException e)
		{
			log.error("error settign qc task to finished state", e);
		}
	}

	private void initiateAutoQc(AttributeMap messageAttributes)
	{
		Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);
		
		try
		{

			try
			{
				AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
				taskController.saveTask(updateMap);
			}
			catch (Exception e)
			{
				log.error("error updating task state", e);
			}

			String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);

			log.info("Initiating qc workflow for asset " + assetID);
			
			String assetTitle = messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
			
			mule.initiateQcWorkflow(assetID, false, taskID.longValue(),assetTitle);
		}
		catch (Exception e)
		{
			log.error("Error initiating auto qc : ", e);
			taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

}
