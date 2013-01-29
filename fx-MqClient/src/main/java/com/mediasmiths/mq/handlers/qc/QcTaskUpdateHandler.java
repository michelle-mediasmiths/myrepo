package com.mediasmiths.mq.handlers.qc;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class QcTaskUpdateHandler extends TaskUpdateHandler
{

	private static final String MANUAL_QC_PASS = "pass";
	private final static String MANUAL_QC_FAIL_WITH_REINGEST = "reingest";
	private final static String MANUAL_QC_FAIL_WITH_REORDER = "reorder";

	private final static Logger log = Logger.getLogger(QcTaskUpdateHandler.class);

	@Inject
	MuleWorkflowController mule;

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

		// qc result set manually
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
			if (result.equals(MANUAL_QC_PASS))
			{
				currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
				currentAttributes.setAttribute(Attribute.QC_STATUS, QcStatus.PASS_MANUAL);
				taskController.saveTask(currentAttributes);
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REORDER))
			{
				currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				currentAttributes.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(currentAttributes);
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REINGEST))
			{
				// user has requested reingest, fail the qc task
				log.debug("User requested uningest, failing qc task");
				currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				currentAttributes.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(currentAttributes);

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
				currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
				taskController.saveTask(currentAttributes);
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
		QcStatus fileFormat = after.getAttribute(Attribute.QC_SUBSTATUS1);

		if (fileFormat.equals(QcStatus.PASS) || fileFormat.equals(QcStatus.PASS_MANUAL))
		{
			boolean autoQcRequired = materialController.isAutoQcRequiredForMaterial(currentAttributes);
			boolean isAutoQcRunOrRunning = materialController.isAutoQcRunOrRunningForMaterial(currentAttributes);

			if (autoQcRequired && !isAutoQcRunOrRunning)
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
				currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
				taskController.saveTask(currentAttributes);
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
			currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
			currentAttributes.setAttribute(Attribute.QC_STATUS, passStatus);
			taskController.saveTask(currentAttributes);
		}
		catch (MayamClientException e)
		{
			log.error("error settign qc task to finished state", e);
		}
	}

	private void initiateAutoQc(AttributeMap messageAttributes)
	{
		try
		{

			try
			{
				messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
				taskController.saveTask(messageAttributes);
			}
			catch (Exception e)
			{
				log.error("error updating task state", e);
			}

			String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);

			log.info("Initiating qc workflow for asset " + assetID);
			Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);
			
			String assetTitle = messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
			
			mule.initiateQcWorkflow(assetID, false, taskID.longValue(),assetTitle);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Inititae QC Message : ", e);
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

}
