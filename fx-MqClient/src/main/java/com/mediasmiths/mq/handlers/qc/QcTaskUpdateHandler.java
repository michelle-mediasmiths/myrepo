package com.mediasmiths.mq.handlers.qc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.ChannelConditionsFound;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class QcTaskUpdateHandler extends TaskUpdateHandler
{

	private static final String QC_FAILED_RE_ORDER = "QcFailedReOrder";
	private static final String CHANNEL_CONDITIONS_FOUND_DURING_QC = "ChannelConditionsFoundDuringQC";
	private static final String MANUAL_QC_PASS = "pass";
	private final static String MANUAL_QC_FAIL_WITH_REINGEST = "reingest";
	private final static String MANUAL_QC_FAIL_WITH_REORDER = "reorder";

	private final static Logger log = Logger.getLogger(QcTaskUpdateHandler.class);

	@Inject
	protected ChannelProperties channelProperties;
	
	@Inject
	MuleWorkflowController mule;

	@Inject(optional = false)
	@Named("qc.events.namespace")
	private String qcEventNamespace;

	@Inject
	@Named("fxcommon.serialiser")
	private JAXBSerialiser serialiser;

	@Override
	public String getName()
	{
		return "QCTaskUpdate";
	}

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{

		// QC Performed in the order
		//
		// 1. File format verification - A check of the technical metadata returned by ardome against preconfigured profiles (set in environment properties)
		// 2. Channel Conditions check - A check of VTR channel conditions, if any are present the asset will be marked as requiring auto qc
		// 3. Auto QC - Cerify, performed if there are channel conditions or the assets qc_required metadata is set. Note VTR is not the only source of channel conditions, assets coming from ruzz may
		// have them also

		// QC_SUBSTATUS1 - File format verification status
		// QC_SUBSTATUS2 - auto qc status
		// QC_SUBSTATUS3 - channel condition status

		// Yes these numbers are in a different order, yes its annoying, be careful!

		// if update was to change substatus for file format verification
		if (after.containsAttribute(Attribute.QC_SUBSTATUS1))
		{
			try
			{
				fileFormatVerificationStatusChanged(currentAttributes, after);
			}
			catch (MayamClientException e)
			{
				log.error("QC : Error processing file format verification status change", e);
			}
		}

		// channel conditions status changed
		if (after.containsAttribute(Attribute.QC_SUBSTATUS3))
		{
			try
			{
				channelConditionStatusChanged(currentAttributes, after);
			}
			catch (MayamClientException e)
			{
				log.error("QC : Error processing channel condition status change", e);
			}
		}

		// autoqc status changed
		if (after.containsAttribute(Attribute.QC_SUBSTATUS2))
		{
			try
			{

				autoQcStatusChanged(currentAttributes, after);
			}
			catch (MayamClientException e)
			{
				log.error("QC : Error processing file  channel condition status change", e);
			}
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
				log.info("QC : Setting QC_STATUS to PASS_MANUAL and TASL_STATE to FINISHED");
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.PASS_MANUAL);
				taskController.saveTask(updateMap);
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REORDER))
			{
				sendQcFailedReorderEvent(currentAttributes);
				log.info("QC : Setting QC_STATUS to FAIL and TASL_STATE to FINISHED_FAILED");
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(updateMap);
			}
			else if (result.equals(MANUAL_QC_FAIL_WITH_REINGEST))
			{
				// user has requested reingest, fail the qc task
				log.debug("QC : User requested uningest, failing qc task");
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED_FAILED);
				updateMap.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
				taskController.saveTask(updateMap);

				if (AssetProperties.isMaterialProgramme(currentAttributes)
						|| AssetProperties.isMaterialAssociated(currentAttributes))
				{
					log.debug("QC : Asset is programme or associated content");
					// uningest the media
					log.debug("QC : User requested uningest, uningesting media");
					materialController.uningest(currentAttributes);

					// send email about QC failed event
					sendQcFailedReorderEvent(currentAttributes);
				}
			}
		}
		catch (MayamClientException e)
		{
			log.error("QC : error updating task status", e);
		}
	}

	private void sendQcFailedReorderEvent(AttributeMap currentAttributes)
	{
		try
		{
			AutoQCFailureNotification aen = new AutoQCFailureNotification();
			aen.setAssetId(currentAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			aen.setForTXDelivery(false);
			aen.setTitle(currentAttributes.getAttributeAsString(Attribute.ASSET_TITLE));

			try
			{
				Set<String> channelGroups = mayamClient.getChannelGroupsForItem(currentAttributes);
				aen.getChannelGroup().addAll(channelGroups);
			}
			catch (Exception e)
			{
				log.error("error determining channel groups for event", e);
			}


			String eventName = QC_FAILED_RE_ORDER;
			String event = serialiser.serialise(aen);
			String namespace = qcEventNamespace;
			
			eventsService.saveEvent(eventName, event, namespace);
		}
		catch (Exception e)
		{
			log.error("QC : error sending " + QC_FAILED_RE_ORDER + " event", e);
		}
	}

	private void fileFormatVerificationStatusChanged(AttributeMap currentAttributes, AttributeMap after)
			throws MayamClientException
	{
		// file format verification status updated
		QcStatus fileFormat = currentAttributes.getAttribute(Attribute.QC_SUBSTATUS1);

		// log the change
		logQcStatusChange(currentAttributes, fileFormat, "file format verification");

		if (fileFormat.equals(QcStatus.PASS) || fileFormat.equals(QcStatus.PASS_MANUAL))
		{
			processChannelConditions(currentAttributes);
		}
		else if (fileFormat.equals(QcStatus.FAIL))
		{
			finishWithWarning(currentAttributes);
		}
	}

	private void channelConditionStatusChanged(AttributeMap currentAttributes, AttributeMap after) throws MayamClientException
	{
		// channel condition status updated
		QcStatus channelConditionStatus = currentAttributes.getAttribute(Attribute.QC_SUBSTATUS3);// it's sub status 3 but it's the second stage of qc (!)
		// log the change
		logQcStatusChange(currentAttributes, channelConditionStatus, "channel condition");

		if (channelConditionStatus.equals(QcStatus.PASS) || channelConditionStatus.equals(QcStatus.PASS_MANUAL))
		{
			boolean autoQcRequired = materialController.isAutoQcRequiredForMaterial(currentAttributes);

			if (autoQcRequired)
			{
				log.info("QC : Asset flagged as requiring autoqc");
				initiateAutoQc(currentAttributes);
			}
			else
			{
				log.info("QC : Asset not flagged as requiring autoqc, finishing qc");
				finishWithPass(currentAttributes, channelConditionStatus);
			}
		}
		else if (channelConditionStatus.equals(QcStatus.FAIL))
		{
			log.info("QC : channel condition step failed, autoqc required");
			initiateAutoQc(currentAttributes);
		}
	}

	private void autoQcStatusChanged(AttributeMap currentAttributes, AttributeMap after) throws MayamClientException
	{
		QcStatus autoQc = after.getAttribute(Attribute.QC_SUBSTATUS2); // it's sub status 2 but it's the third stage of qc (!)
		// log the change
		logQcStatusChange(currentAttributes, autoQc, "autoqc");

		if (autoQc.equals(QcStatus.PASS) || autoQc.equals(QcStatus.PASS_MANUAL))
		{
			finishWithPass(currentAttributes, autoQc);
		}
		else if (autoQc.equals(QcStatus.FAIL))
		{
			finishWithWarning(currentAttributes);
		}
	}

	private void processChannelConditions(AttributeMap currentAttributes)
	{
		try
		{
			String assetID = (String) currentAttributes.getAttribute(Attribute.ASSET_ID);
			log.debug("QC : searching for channel conditions for assset " + assetID);
			List<String> conditions = tasksClient.assetApi().getQcMessages(
					(AssetType) currentAttributes.getAttribute(Attribute.ASSET_TYPE),
					assetID);

			boolean isConditions = false;
			
			if(conditions!=null && !conditions.isEmpty()){
				
				for (String condition : conditions)
				{
					log.debug(String.format("condition {%s}", condition));
				}
				
				if(conditions.size() > 0){
					if(! (( conditions.size()==1) && conditions.get(0).equals(""))){ //sometimes getQCMessages returns a single empty string, assume that means no channel conditions
						isConditions=true;
					}
				}
			}
			
			if (isConditions)
			{
				log.info(String.format("QC : %d conditions returned for asset", conditions.size()));
				String stConditions = StringUtils.join(conditions, '\n');
				log.info("QC : Channel conditions for asset are: " + stConditions);
				log.debug("QC : Updating qc substatus and notes for channel conditions");
				
				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
				updateMap.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.FAIL);
				updateMap.setAttribute(Attribute.QC_SUBSTATUS3_NOTES, "Channel conditions detected, sending to auto QC");
				taskController.saveTask(updateMap);
				
				//create channel conditions found during qc event
				ChannelConditionsFound ccf = new ChannelConditionsFound();
				ccf.setMaterialID(currentAttributes.getAttributeAsString(Attribute.HOUSE_ID));
				
				try
				{
					Set<String> channelGroups = mayamClient.getChannelGroupsForItem(currentAttributes);
					ccf.getChannelGroup().addAll(channelGroups);
				}
				catch (Exception e)
				{
					log.error("error determinging channel groups for event", e);
				}

				
				log.debug("saving event "+CHANNEL_CONDITIONS_FOUND_DURING_QC);
				eventsService.saveEvent(qcEventNamespace, CHANNEL_CONDITIONS_FOUND_DURING_QC, ccf);				
			}
			else
			{
				log.info("QC : No channel conditions returned for asset");
				log.debug("QC : Updating qc substatus and notes for channel conditions");
				
				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
				updateMap.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.PASS);
				updateMap.setAttribute(Attribute.QC_SUBSTATUS3_NOTES, "");
				taskController.saveTask(updateMap);
			}
		}
		catch (Exception e)
		{
			log.error("QC : Error processing channel conditions", e);
			taskController.setTaskToWarningWithMessage(currentAttributes, "Error processing channel conditions");
		}
	}

	private void logQcStatusChange(AttributeMap currentAttributes, QcStatus autoQc, String description)
	{
		String houseID = currentAttributes.getAttributeAsString(Attribute.HOUSE_ID);
		String assetID = currentAttributes.getAttributeAsString(Attribute.ASSET_ID);
		log.info(String.format("QC : Asset %s (%s) %s status has changed to %s", houseID, assetID, description, autoQc.toString()));
	}

	private void finishWithPass(AttributeMap currentAttributes, QcStatus passStatus) throws MayamClientException
	{
		log.info("QC : About to try to update qc task state to FINISHED");
		AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
		updateMap.setAttribute(Attribute.QC_STATUS, passStatus);
		taskController.saveTask(updateMap);
	}

	private void finishWithWarning(AttributeMap currentAttributes) throws MayamClientException
	{
		log.info("QC : About to try to update qc task state to WARNING");
		AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.WARNING);
		taskController.saveTask(updateMap);
		sendQcFailedReorderEvent(currentAttributes);
	}

	private void initiateAutoQc(AttributeMap messageAttributes)
	{
		Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);

		String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		log.info("QC : Initiating qc workflow for asset " + assetID);

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
				log.error("QC : error updating task state", e);
			}

			String assetTitle = messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE);

			mule.initiateQcWorkflow(assetID, false, taskID.longValue(), assetTitle);
		}
		catch (Exception e)
		{
			log.error("QC : Error initiating auto qc : ", e);
			taskController.setTaskToWarningWithMessage(messageAttributes, "Error initiating tx workflow");
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

}
