package com.mediasmiths.mq.handlers.export;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.extendedpublishing.ExtendedPublishingProperties;
import com.mediasmiths.foxtel.extendedpublishing.OutputPaths;
import com.mediasmiths.foxtel.extendedpublishing.TCJobParamsGenerator;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
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
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

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

		log.debug("Initiating export for asset" +materialID);

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

		boolean isDVD = ExtendedPublishingProperties.isDVD(messageAttributes);

		try
		{
			if (!prepareDestination(outputFileName, channel, jobType, taskID, isDVD))
			{
				log.error("Failed to prepare destination");
				return;
			}
		}
		catch (Exception e)
		{
			log.error("Error deleting files at export destination or creating folder for dvd export", e);
			taskController.setTaskToErrorWithMessage(taskID,
			                                         "Error delete files at export destination or creating folder for dvd export");
			return;
		}

		// invoke export flow
		try
		{
			initiateWorkflow(title, materialID, packageID, jobParams, taskID, jobType);
			ExportStart export = new ExportStart();
			export.setMaterialID(materialID);
			export.setChannels(channel);
			eventService.saveEvent("http://www.foxtel.com.au/ip/tc", EventNames.EXPORT_START, export);
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


	/**
	 * Creates destination folder for dvd exports, if it already exists then it is cleared
	 * <p/>
	 * If export is for a single file then if the target already exists it is deleted
	 *
	 * @param outputFileName
	 * @param channel
	 * @param jobType
	 * @param taskID
	 * @param isDVD
	 *
	 * @return
	 */
	private boolean prepareDestination(final String outputFileName,
	                                   final String channel,
	                                   final TranscodeJobType jobType,
	                                   final Long taskID,
	                                   final boolean isDVD)
	{
		String localPathToExportDestination = outputPaths.getLocalPathToExportDestination(channel,
		                                                                                  jobType,
		                                                                                  outputFileName,
		                                                                                  isDVD);
		if (isDVD)
		{
			// we need to make the output folder on the corporate storage because rhozet wont
			File folder = new File(localPathToExportDestination);
			if (folder.exists())
			{
				if(folder.isDirectory()){

					try
					{
						log.debug(String.format("Destination folder %s exists, cleaning", folder.getAbsolutePath()));
						FileUtils.cleanDirectory(folder);
					}
					catch (IOException e)
					{
						log.error("Error removing existing file at DVD export destination : " + localPathToExportDestination);
						taskController.setTaskToErrorWithMessage(taskID,
						                                         "Error removing existing file at DVD export destination");
						return false;
					}
				}
				else
				{
					//there is a file where we were expecting a folder
					if (!FileUtils.deleteQuietly(folder))
					{
						log.error("Error removing existing file at export destination : " + localPathToExportDestination);
						taskController.setTaskToErrorWithMessage(taskID, "Error removing existing file at export destination");
						return false;
					}

					if (createFolderForDVDOutput(taskID, localPathToExportDestination, folder))
					{
						return true;
					}
				}
			}
			else
			{
				if (createFolderForDVDOutput(taskID, localPathToExportDestination, folder))
				{
					return true;
				}
			}
		}
		else
		{
			//Check if file already exists in output location and remove to avoid conflicts
			boolean fileExists = outputPaths.fileExistsAtExportDestination(channel, jobType, outputFileName, isDVD);

			if (fileExists)
			{
				log.info(String.format("Destination file %s already exists, deleting", localPathToExportDestination));
				File outputFile = new File(localPathToExportDestination);

				if (!FileUtils.deleteQuietly(outputFile))
				{
					log.error("Error removing existing file at export destination : " + localPathToExportDestination);
					taskController.setTaskToErrorWithMessage(taskID, "Error removing existing file at export destination");
					return false;
				}
			}
		}
		return true;
	}


	private boolean createFolderForDVDOutput(final Long taskID, final String localPathToExportDestination, final File folder)
	{
		if (!folder.mkdirs())
		{
			log.error("Error creating folder at export destination : " + localPathToExportDestination);
			taskController.setTaskToErrorWithMessage(taskID, "Error creating folder at export destination");
			return true;
		}
		return false;
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
