package com.mediasmiths.mq.handlers.tx;

import java.io.File;
import java.io.IOException;
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
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.priorities.TranscodePriorities;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.transcode.TranscodeRules;
import com.mediasmiths.foxtel.tx.ftp.TxFtpDelivery;
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

	@Inject
	private TranscodePriorities transcodePriorities;
	
	@Override
	public String getName()
	{
		return "Initiate TX Delivery";
	}
	
	@Inject
	TranscodeRules transcodeOutputRules;

	
	//ftp related members are for checking if tx delivery output already exists for ao content
	@Inject
	TxFtpDelivery ftpDelivery;
	
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.host")
	private String aoGXFFTPDestinationHost;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.user")
	private String aoGXFFTPDestinationUser;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.pass")
	private String aoGXFFTPDestinationPass;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.path")
	private String aoGXFFTPDestinationPath;
	
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
		else if (!messageAttributes.getAttribute(Attribute.TASK_STATE).equals(TaskState.SYS_WAIT))
		{
			//If in Sys Wait state then ignore until task is reopened after high res transfer is complete
			
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
								
				boolean fxpFileExists = false;
				
				if (isAO)
				{
					try
					{
						fxpFileExists = ftpDelivery.fileExists(
								aoGXFFTPDestinationPath,
								packageID,
								aoGXFFTPDestinationHost,
								aoGXFFTPDestinationUser,
								aoGXFFTPDestinationPass);
					}
					catch (Exception e)
					{
						log.error("Error querying ao output ftp location", e);
						taskController.setTaskToErrorWithMessage(messageAttributes, "Error querying ao output ftp location");
						return;
					}
				}

				if (fxpFileExists || essenceFile.exists() || companionFile.exists())
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
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error initiating tx workflow");
			}
			catch (UnsupportedEncodingException e)
			{
				log.error("error invoking tx delivery", e);
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error initiating tx workflow");
			}
			catch (JAXBException e)
			{
				log.error("error invoking tx delivery", e);
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error initiating tx workflow");
			}
			catch(IOException e){
				log.error("Error querying ao output ftp location", e);
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error querying ao output ftp location");
			}
			catch (MayamClientException e)
			{
				
				if (MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED.equals(e.getErrorcode()))
				{
					log.error("Error getting material's location", e);
					taskController.setTaskToErrorWithMessage(messageAttributes, "Error getting material's location");
				}
				else if (MayamClientErrorCode.FILE_LOCATON_UNAVAILABLE.equals(e.getErrorcode())
						|| MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION.equals(e.getErrorcode()))
				{
					log.warn("Material unavailable or not found on hires storage", e);
					materialController.initiateHighResTransfer(messageAttributes);
				}
				else
				{
					log.error("error initiating tx delivery", e);
					taskController.setTaskToErrorWithMessage(messageAttributes, "Error initiating tx workflow");
				}
			}
			catch (MuleException e)
			{
				log.error("error initiating tx delivery",e);
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error initiating tx workflow");
			}
			catch(Throwable t)
			{
				log.error("Error initiating tx delivery", t);
				taskController.setTaskToErrorWithMessage(messageAttributes,  "Error initiating tx workflow");
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

		ret.audioType = transcodeOutputRules.audioTypeForTranscode(materialIsSD, materialIsSurround, isPackageSD);
		
		// no bug for tx delivery
		ret.bug = null;
		ret.description = String.format("TX Delivery for package %s", packageID);

		ret.inputFile = materialPath;
		ret.outputFileBasename = packageID;
		ret.outputFolder = outputLocation;
		ret.priority = getPriorityForTXJob(requiredDate);

		if(materialIsSD){
			ret.resolution = TCResolution.SD;
		}
		else{
			ret.resolution = TCResolution.HD;
		}
		
		if (isPackageSD)
		{
			ret.purpose = TCOutputPurpose.TX_SD;
		}
		else
		{
			ret.purpose = TCOutputPurpose.TX_HD;
			ret.resolution = TCResolution.HD; //people shouldnt be up converting but in case they are then this is set to HD for HD outputs
			
			if(materialIsSD)
			{
				log.warn("An upscale has been requested!");
			}
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
		startMessage.setCreated(new Date());

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
		Integer priority = transcodePriorities.getPriorityForNewTranscodeJob(TranscodeJobType.TX, txDate);
		log.debug("priority is "+priority.intValue());
		return priority.intValue();
	}

}
