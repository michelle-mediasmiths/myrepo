package com.mediasmiths.mq.handlers.export;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.extendedpublishing.OutputPaths;
import com.mediasmiths.foxtel.extendedpublishing.TCJobParamsGenerator;
import com.mediasmiths.foxtel.ip.common.events.ExportStart;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeExport;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateExportHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(InitiateExportHandler.class);

	@Inject
	private MuleWorkflowController mule;

	@Inject
	private EventService eventService;

	@Inject
	private TCJobParamsGenerator tcJobsParamGenerator;
	
	@Inject
	OutputPaths outputPaths;

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		// an export task has been created, build tc params and invoke intalio workflow.
		String outputFileName = (String) messageAttributes.getAttribute(Attribute.OP_FILENAME);
		String buglocation = (String) messageAttributes.getAttribute(Attribute.VISUAL_BUG);
		String timecodePosition = (String) messageAttributes.getAttribute(Attribute.VISUAL_TIMECODE_POSITION);
		String timecodeColour = (String) messageAttributes.getAttribute(Attribute.VISUAL_TIMECODE_COLOR);
		String requestedFormat = (String) messageAttributes.getAttribute(Attribute.OP_FMT);
		String channel = (String) messageAttributes.getAttribute(Attribute.CHANNEL);

		String materialID;
		String packageID;

		AttributeMap materialAttributes;
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
		{
			log.debug("export is for a package");
			materialID = (String) messageAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
			packageID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
			materialAttributes = materialController.getMaterialAttributes(materialID);
		}
		else if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			materialAttributes = messageAttributes;
			materialID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
			packageID = null;
		}
		else
		{
			log.error("unexpected asset type");
			throw new IllegalArgumentException("can only export items or packages");
		}

		Date firstTX = (Date) messageAttributes.getAttribute(Attribute.TX_FIRST); // package first tx date
		if (firstTX == null)
		{
			log.warn("null firstTX date!");
		}

		boolean isSurround = AssetProperties.isMaterialSurround(materialAttributes);
		boolean isSD = AssetProperties.isMaterialSD(materialAttributes);
		String title = (String) materialAttributes.getAttribute(Attribute.ASSET_TITLE);
		TranscodeJobType jobType = TranscodeJobType.fromText((String) messageAttributes.getAttribute(Attribute.OP_TYPE));

		// If 'No Bug' has been selected then ignore any text in the bug location field
		Boolean noBug = messageAttributes.getAttribute(Attribute.VISUAL_BUG_FLAG);
		log.debug("visual bug flag: " + messageAttributes.getAttributeAsString(Attribute.VISUAL_BUG_FLAG));
		log.debug("buglocation : " + buglocation);
		if (noBug != null && (noBug.booleanValue() == true))
		{
			log.debug("'no bug' option was selected");
			buglocation = null;
		}
		log.debug("buglocation : " + buglocation);

		Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);

		String inputFile = null;

		try
		{
			inputFile = mayamClient.pathToMaterial(materialID, false);
		}
		catch (MayamClientException e)
		{
			if (MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED.equals(e.getErrorcode()))
			{
				log.error("Error getting material's location", e);
				taskController.setTaskToErrorWithMessage(messageAttributes, "Error getting material's location");
				return;
			}
			else if (MayamClientErrorCode.FILE_LOCATON_UNAVAILABLE.equals(e.getErrorcode())
					|| MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION.equals(e.getErrorcode()))
			{
				log.warn("Material unavailable or not found on hires storage, attempting to initiate transfer", e);
				materialController.initiateHighResTransfer(messageAttributes);
				
				return;
			}
		}

		// construct transcode job parameters
		TCJobParameters jobParams;
		try
		{
			jobParams = tcJobsParamGenerator.jobParams(
					isSurround,
					isSD,
					outputFileName,
					buglocation,
					timecodePosition,
					timecodeColour,
					materialID,
					channel,
					taskID,
					firstTX,
					jobType,
					inputFile,
					requestedFormat);
		}
		catch (Exception e)
		{
			log.error("error constructing job params for export proxy", e);
			taskController.setTaskToErrorWithMessage(taskID, e.getMessage());
			return;
		}
		
		boolean isDVD = false;
		if ("dvd".equals(requestedFormat)){
			isDVD = true;
		}
		
		String localPathToExportDestination = outputPaths.getLocalPathToExportDestination(
				channel,
				jobType,
				outputFileName,
				isDVD);
		
		if (isDVD)
		{
			// we need to make the output folder on the corporate storage because rhozet wont
			File folder = new File(localPathToExportDestination);
			if (folder.exists())
			{
				File[] files = folder.listFiles();
				if (files != null)
				{
					for (File file: files)
					{
						if (file.isDirectory())
						{
							try {
								FileUtils.deleteDirectory(file);
							}
							catch(IOException e)
							{
								log.error("Error removing existing file at DVD export destination : "+localPathToExportDestination);
								taskController.setTaskToErrorWithMessage(taskID, "Error removing existing file at DVD export destination");
								return;
							}
						}
						else {
							if (!file.delete())
							{
								log.error("Error removing existing file at DVD export destination : "+localPathToExportDestination);
								taskController.setTaskToErrorWithMessage(taskID, "Error removing existing file at DVD export destination");
								return;
							}
						}
					}
				}
			}
			else {
				if (!folder.mkdirs())
				{
					log.error("Error creating folder at export destination : "+localPathToExportDestination);
					taskController.setTaskToErrorWithMessage(taskID, "Error creating folder at export destination");
					return;
				}
			}
		}
		else {
			//Check if file already exists in output location and remove to avoid conflicts
			boolean fileExists = outputPaths.fileExistsAtExportDestination(
					channel,
					jobType,
					outputFileName,
					isDVD);
			

			if (fileExists)
			{
				File outputFile = new File(localPathToExportDestination);
				if (!outputFile.delete())
				{
					log.error("Error removing existing file at export destination : "+localPathToExportDestination);
					taskController.setTaskToErrorWithMessage(taskID, "Error removing existing file at export destination");
					return;
				}
			}
			
		}

		// invoke export flow
		try
		{
			initiateWorkflow(title, materialID, packageID, jobParams, taskID, jobType);
			ExportStart export = new ExportStart();
			export.setMaterialID(materialID);
			export.setChannels(channel);
			eventService.saveEvent("http://www.foxtel.com.au/ip/tc", "ExportStart", export);
		}
		catch (UnsupportedEncodingException e)
		{
			log.error("error initiating export workflow", e);
			taskController.setTaskToErrorWithMessage(taskID, "Error initiating export workflow");
			return;
		}
		catch (JAXBException e)
		{
			log.error("error initiating export workflow", e);
			taskController.setTaskToErrorWithMessage(taskID, "Error initiating export workflow");
			return;
		}
		catch (MuleException e)
		{
			log.error("error initiating export workflow", e);
			taskController.setTaskToErrorWithMessage(taskID, "Error initiating export workflow");
			return;
		}

		// set task to active state
		try
		{
			AttributeMap task;
			task = taskController.getTask(taskID);
			AttributeMap updateMap = taskController.updateMapForTask(task);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
			taskController.saveTask(updateMap);
		}
		catch (RemoteException e)
		{
			log.error("error setting task " + taskID + " to state ACTIVE", e);
		}
		catch (MayamClientException e)
		{
			log.error("error setting task " + taskID + " to state ACTIVE", e);
		}

	}

	private void initiateWorkflow(
			String assetTitle,
			String materialID,
			String packageID,
			TCJobParameters jobParams,
			long taskID,
			TranscodeJobType jobType) throws UnsupportedEncodingException, JAXBException, MuleException
	{
		InvokeExport ie = new InvokeExport();
		ie.setAssetID(materialID);
		ie.setTaskID(taskID);
		ie.setTcParams(jobParams);
		ie.setTitle(assetTitle);
		ie.setCreated(new Date());
		ie.setJobType(jobType.getText());

		if (packageID == null || "".equals(packageID))
		{
			log.debug("package id is null");
			ie.setPackageID("NA");
		}
		else
		{
			ie.setPackageID(packageID);
		}

		mule.initiateExportWorkflow(ie);
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.EXTENDED_PUBLISHING;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	@Override
	public String getName()
	{
		return "Initiate Export";
	}

}
