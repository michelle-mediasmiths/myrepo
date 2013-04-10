package com.mediasmiths.mq.handlers.tx;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.activemq.thread.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.DateUtil;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.foxtel.wf.adapter.util.TxUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateTxHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(InitiateTxHandler.class);

	@Inject
	private MuleWorkflowController mule;

	@Inject
	@Named("tx.delivery.location")
	private String txDeliveryLocation;


	@Inject
	@Named("ao.tx.delivery.location")
	private String aoDeliveryLocation;
	
	@Override
	public String getName()
	{
		return "Initiate TX Delivery";
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{

		if (!AssetProperties.isPackageTXReady(messageAttributes))
		{
			AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REJECTED);
			updateMap.setAttribute(Attribute.ERROR_MSG, "Package is not TX Ready");
			try
			{
				taskController.saveTask(updateMap);
			}
			catch (MayamClientException e)
			{
				log.error("error rejecting tx delivery task",e);
			}
		}
		else
		{

			Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);

			
			try
			{
				String packageID = messageAttributes.getAttribute(Attribute.HOUSE_ID);			
				SegmentList segmentList = packageController.getSegmentList(packageID);
				String materialID = segmentList.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
				AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);

				boolean isAO = AssetProperties.isAO(materialAttributes);
				boolean materialIsSurround = AssetProperties.isMaterialSurround(materialAttributes);
				boolean materialIsSD = AssetProperties.isMaterialSD(materialAttributes);
				boolean isPackageSD = AssetProperties.isPackageSD(segmentList.getAttributeMap());

				String title = materialAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
				Date requiredDate = (Date) segmentList.getAttributeMap().getAttribute(Attribute.TX_FIRST);

				if (requiredDate == null)
				{
					log.warn("No required date set on package! " + packageID);
					requiredDate = new Date(Date.UTC(3000, 1, 1, 0, 0, 0));
					log.info("Using required date " + requiredDate);
				}

				String materialPath = mayamClient.pathToMaterial(materialID,false);				
				String outputLocation = TxUtil.deliveryLocationForPackage(packageID, mayamClient, txDeliveryLocation, aoDeliveryLocation, isAO);

				TCJobParameters tcParams = createTCParamsForTxDelivery(
						packageID,
						materialIsSD,
						materialIsSurround,
						isPackageSD,
						requiredDate,
						materialPath,
						outputLocation);

				String essenceFilePath = tcParams.outputFolder + "/" + tcParams.outputFileBasename + ".gxf";
				String companionFilePath = tcParams.outputFolder + "/" + tcParams.outputFileBasename + ".xml";
				File essenceFile = new File(essenceFilePath);
				File companionFile = new File(companionFilePath);
				
				if (essenceFile.exists() || companionFile.exists())
				{
					String errorMessage = "File already exists at tx delivery target, will not attempt tx delivery";
					log.error(errorMessage);

					AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
					updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
					updateMap.setAttribute(Attribute.ERROR_MSG, errorMessage);
					taskController.saveTask(updateMap);
				}
				else
				{
					startTXFlow(isAO, packageID, requiredDate, taskID, tcParams, title);

					AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
					updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
					taskController.saveTask(updateMap);
				}

			}
			catch (PackageNotFoundException pnfe)
			{
				log.error("package not found when attempting to initiate tx delivery!", pnfe);
				taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
			}
			catch (UnsupportedEncodingException e)
			{
				log.error("error invoking tx delivery", e);
				taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
			}
			catch (JAXBException e)
			{
				log.error("error invoking tx delivery", e);
				taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
			}
			catch (MayamClientException e)
			{
				
				if (MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED.equals(e.getErrorcode()))
				{
					log.error("Error getting material's location", e);
					taskController.setTaskToErrorWithMessage(taskID, "Error getting material's location");
				}
				else if (MayamClientErrorCode.FILE_LOCATON_UNAVAILABLE.equals(e.getErrorcode())
						|| MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION.equals(e.getErrorcode()))
				{
					log.error("Material unavailable or not found on hires storage", e);
					taskController.setTaskToErrorWithMessage(taskID, "Material unavailable or not on Hires storage");
				}
				else
				{
					log.error("error initiating tx delivery", e);
					taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
				}
			}
			catch (MuleException e)
			{
				log.error("error initiating tx delivery",e);
				taskController.setTaskToErrorWithMessage(taskID, "Error initiating tx workflow");
			}

		}
	}

	private TCJobParameters createTCParamsForTxDelivery(
			String packageID,
			boolean materialIsSD,
			boolean materialIsSurround,
			boolean isPackageSD,
			Date requiredDate,
			String materialPath,
			String outputLocation)
	{
		TCJobParameters ret = new TCJobParameters();

		if (materialIsSurround)
		{
			ret.audioType = TCAudioType.DOLBY_E;
		}
		else
		{
			ret.audioType = TCAudioType.STEREO;
		}

		// no bug for tx delivery
		ret.bug = null;
		ret.description = String.format("TX Delivery for package %s", packageID);

		ret.inputFile = materialPath;
		ret.outputFileBasename = packageID;
		ret.outputFolder = outputLocation;
		ret.priority = getPriorityForTXJob(requiredDate);

		if (isPackageSD)
		{
			ret.purpose = TCOutputPurpose.TX_SD;
			ret.resolution = TCResolution.SD;
		}
		else
		{
			ret.purpose = TCOutputPurpose.TX_HD;
			ret.resolution = TCResolution.HD;
		}

		// no timecode for tx delivery
		ret.timecode = null;

		return ret;
	}

	private void startTXFlow(
			boolean isAO,
			String packageID,
			Date requiredDate,
			Long taskID,
			TCJobParameters tcParams,
			String title) throws UnsupportedEncodingException, JAXBException, MuleException
	{
		InvokeIntalioTXFlow startMessage = new InvokeIntalioTXFlow();

		startMessage.setAO(isAO);
		startMessage.setPackageID(packageID);
		startMessage.setRequiredDate(requiredDate);
		startMessage.setTaskID(taskID);
		startMessage.setTcParams(tcParams);
		startMessage.setTitle(title);

		mule.initiateTxDeliveryWorkflow(startMessage);

	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.TX_DELIVERY;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	private final static long ONE_HOUR = 1000 * 3600l;
	private final static long TWELVE_HOURS = ONE_HOUR * 12;
	private final static long ONE_DAY = ONE_HOUR * 24;
	private final static long THREE_DAYS = ONE_DAY * 3;
	private final static long SEVEN_DAYS = ONE_DAY * 7;
	private final static long EIGHT_DAYS = ONE_DAY * 8;

	public int getPriorityForTXJob(Date txDate)
	{
		log.debug(String.format("determining prority for job for asset tx date is %s", txDate.toString()));

		int priority = 1;

		long now = System.currentTimeMillis();
		long txTime = txDate.getTime();
		long difference = txTime - now;

		if (difference > 0)
		{
			// tx date is in the future
			if (difference < TWELVE_HOURS)
			{
				priority = 8;
			}
			else if (difference < ONE_DAY)
			{
				priority = 7;
			}
			else if (difference < THREE_DAYS)
			{
				priority = 6;
			}
			else if (difference < EIGHT_DAYS)
			{
				priority = 4;
			}
			else
			{
				priority = 2;
			}
		}
		else
		{
			// tx date is in the past
			if (Math.abs(difference) <= SEVEN_DAYS)
			{
				priority = 8; // go to the highest priority for this destination if the target date is no more than 7 days in the past
			}
			else
			{
				priority = 2;// else content goes to the lowest priority queue for that destination
			}
		}

		log.debug("returning prority " + priority);
		return priority;
	}

}
