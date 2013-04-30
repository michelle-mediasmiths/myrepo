package com.mediasmiths.mq.handlers.export;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.common.events.ExportStart;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.priorities.TranscodePriorities;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCLocation;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeColour;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeOptions;
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
	@Named("export.ftp.user")
	private String exportFTPUser;

	@Inject
	@Named("export.ftp.password")
	private String exportFTPPassword;

	@Inject
	@Named("export.ftp.server")
	private String exportFTPServer;

	@Inject
	@Named("export.transient.tc.output.location")
	// where transcoded files go before ftp upload + deletion
	private String exportOutputLocation;

	@Inject
	protected ChannelProperties channelProperties;

	@Inject
	private EventService eventService;

	@Inject
	private TranscodePriorities transcodePriorities;

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		// an export task has been created, build tc params and invoke intalio workflow.
		String outputFileName = (String) messageAttributes.getAttribute(Attribute.OP_FILENAME);
		String buglocation = (String) messageAttributes.getAttribute(Attribute.VISUAL_BUG);
		String timecodePosition = (String) messageAttributes.getAttribute(Attribute.VISUAL_TIMECODE_POSITION);
		String timecodeColour = (String) messageAttributes.getAttribute(Attribute.VISUAL_TIMECODE_COLOR);

		StringList channels = messageAttributes.getAttribute(Attribute.CHANNELS);
		
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
			packageID=null;
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
				materialController.initiateHighResTransfer(messageAttributes);;
				return;
			}
		}

		// construct transcode job parameters
		TCJobParameters jobParams;
		try
		{
			jobParams = jobParams(
					isSurround,
					isSD,
					outputFileName,
					buglocation,
					timecodePosition,
					timecodeColour,
					channels,
					materialID,
					materialAttributes,
					taskID,
					firstTX,
					jobType,
					inputFile);
		}
		catch (MayamClientException e)
		{
			log.error("error constructing job params for export proxy", e);
			taskController.setTaskToErrorWithMessage(taskID, "Error constructing transcode paramters");
			return;
		}
		catch (Exception e)
		{
			log.error("error constructing job params for export proxy", e);
			taskController.setTaskToErrorWithMessage(taskID, e.getMessage());
			return;
		}

		// invoke export flow
		try
		{
			initiateWorkflow(title, materialID, packageID, jobParams, taskID, jobType);
			ExportStart export = new ExportStart();
			export.setMaterialID(materialID);
			export.setChannels(channels.toString());
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

	private TCJobParameters jobParams(
			boolean isSurround,
			boolean isSD,
			String outputFileName,
			String buglocation,
			String timecodePosition,
			String timecodeColour,
			StringList channels,
			String materialID,
			AttributeMap materialAttributes,
			long taskID,
			Date firstTX,
			TranscodeJobType jobType,
			String inputFile) throws MayamClientException
	{
		TCJobParameters jobParams = new TCJobParameters();
		String channel = null;

		// get the first channel
		if (channels != null && channels.get(0) != null)
		{
			String firstChannel = channels.get(0);
			channel = firstChannel;
		}
		else
		{
			throw new IllegalArgumentException("no channels in asset metadata");
		}

		log.debug("buglocation: " + buglocation);
		if (buglocation != null && !buglocation.equals("--"))
		{
			TCBugOptions bug = bug(buglocation, channel);
			jobParams.bug = bug;
		}

		if (isSurround)
		{
			jobParams.audioType = TCAudioType.DOLBY_E;
		}
		else
		{
			jobParams.audioType = TCAudioType.STEREO;
		}

		if (isSD)
		{
			jobParams.resolution = TCResolution.SD;
		}
		else
		{
			jobParams.resolution = TCResolution.HD;
		}

		String separatorAndExtension = getOutputFileExtension(jobType);

		if (outputFileName != null)
		{
			jobParams.outputFileBasename = outputFileName + separatorAndExtension;
		}
		else
		{
			jobParams.outputFileBasename = materialID + separatorAndExtension;
		}

		jobParams.timecode = timecode(timecodeColour, timecodePosition);
		jobParams.purpose = getPurpose(jobType);

		jobParams.priority = getPriority(firstTX, jobType);

		jobParams.inputFile = inputFile;
		jobParams.outputFolder = exportOutputLocation + "/" + taskID;

		jobParams.ftpupload = new TCFTPUpload();
		jobParams.ftpupload.filename = jobParams.outputFileBasename;
		jobParams.ftpupload.folder = getTranscodeDestination(materialAttributes, jobType);
		jobParams.ftpupload.user = exportFTPUser;
		jobParams.ftpupload.password = exportFTPPassword;
		jobParams.ftpupload.server = exportFTPServer;

		return jobParams;
	}

	private TCOutputPurpose getPurpose(TranscodeJobType jobType)
	{
		switch (jobType)
		{
			case CAPTION_PROXY:
				{
					return TCOutputPurpose.CAPTIONING;
				}
			case COMPLIANCE_PROXY:
				{
					return TCOutputPurpose.MPG4;
				}
			case PUBLICITY_PROXY:
				{
					return TCOutputPurpose.MPG4;
				}
			case TX:
				throw new IllegalArgumentException("Unexpected job type");
			default:
				throw new IllegalArgumentException("Unexpected job type");

		}
	}

	private TCLocation timeCodeLocation(String timecodePosition)
	{
		if (timecodePosition.equals("Bottom"))
		{
			return TCLocation.BOTTOM;
		}
		else if (timecodePosition.equals("Top"))
		{
			return TCLocation.TOP;
		}

		throw new IllegalArgumentException("Unknown timecode position " + timecodePosition);
	}

	private TCTimecodeColour timeCodeColour(char charAt)
	{
		if (charAt == 'W' || charAt == 'w')
		{
			return TCTimecodeColour.WHITE;
		}
		else if (charAt == 'B' || charAt == 'b')
		{
			return TCTimecodeColour.BLACK;
		}
		else if (charAt == 'T' || charAt == 't')
		{
			return TCTimecodeColour.TRANSPARENT;
		}

		throw new IllegalArgumentException("Unknown timecode colour");
	}

	private TCBugOptions bug(String buglocation, String channel)
	{
		// a bug location has been specified
		TCBugOptions bug = new TCBugOptions();
		bug.channel = channel;
		bug.position = location(buglocation);
		return bug;
	}

	private TCLocation location(String location)
	{
		if (location.equals("TL"))
		{
			return TCLocation.TOP_LEFT;
		}
		else if (location.equals("TR"))
		{
			return TCLocation.TOP_RIGHT;
		}
		else if (location.equals("BL"))
		{
			return TCLocation.BOTTOM_LEFT;
		}
		else if (location.equals("BR"))
		{
			return TCLocation.BOTTOM_RIGHT;
		}
		else if (location.equals("T"))
		{
			return TCLocation.TOP;
		}
		else if (location.equals("B"))
		{
			return TCLocation.BOTTOM;
		}

		throw new IllegalArgumentException("unrecognised bug location");
	}

	protected String getExportLocationForFirstChannel(AttributeMap materialAttributes)
	{
		StringList channels = materialAttributes.getAttribute(Attribute.CHANNELS);

		if (channels.size() == 0)
		{
			throw new IllegalArgumentException("no channels found for material, cannot pick export location");
		}

		String channelTag = channels.get(0);
		String channelGroup = channelProperties.channelGroupForChannel(channelTag);
		String exportLocation = channelProperties.exportPathForChannelGroup(channelGroup);
		return exportLocation;

	}

	private TCTimecodeOptions timecode(String timecodeColour, String timecodePosition)
	{

		if (timecodeColour != null && timecodeColour.length() == 3 && timecodePosition != null)
		{
			TCTimecodeOptions ret = new TCTimecodeOptions();
			ret.background = timeCodeColour(timecodeColour.charAt(2));
			ret.foreground = timeCodeColour(timecodeColour.charAt(0));
			ret.location = timeCodeLocation(timecodePosition);
			return ret;
		}
		else
		{
			log.warn(String.format(
					"No timecode options specified or timecode options were invalid color: %s position %s",
					timecodeColour,
					timecodePosition));
		}

		return null;
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
		ie.setPackageID(packageID);

		mule.initiateExportWorkflow(ie);
	}

	@Inject
	@Named("export.caption.path.prefix")
	private String captionOutputPath;
	@Inject
	@Named("export.caption.extention")
	private String captionOutputExtension;

	@Inject
	@Named("export.compliance.foldername")
	private String complianceOutputFolderName;

	@Inject
	@Named("export.compliance.path.prefix")
	private String complianceOutputPrefix;

	@Inject
	@Named("export.compliance.extention")
	private String complianceOutputExtension;

	@Inject
	@Named("export.publicity.foldername")
	private String publicityOutputFolderName;

	@Inject
	@Named("export.publicity.path.prefix")
	private String publicityOutputPrefix;

	@Inject
	@Named("export.publicity.extention")
	private String publicityOutputExtension;

	protected String getTranscodeDestination(AttributeMap materialAttributes, TranscodeJobType jobType)
	{

		switch (jobType)
		{
			case CAPTION_PROXY:
				{
					return captionOutputPath;
				}
			case COMPLIANCE_PROXY:
				{
					String exportLocation = getExportLocationForFirstChannel(materialAttributes);
					return String.format("%s/%s/%s", complianceOutputPrefix, exportLocation, complianceOutputFolderName);
				}
			case PUBLICITY_PROXY:
				{
					String exportLocation = getExportLocationForFirstChannel(materialAttributes);
					return String.format("%s/%s/%s", publicityOutputPrefix, exportLocation, publicityOutputFolderName);
				}
			case TX:
				throw new IllegalArgumentException("Unexpected job type");
			default:
				throw new IllegalArgumentException("Unexpected job type");

		}
	}

	protected String getOutputFileExtension(TranscodeJobType jobType)
	{
		switch (jobType)
		{
			case CAPTION_PROXY:
				{
					return String.format(".%s",captionOutputExtension);
				}
			case COMPLIANCE_PROXY:
				{
					return String.format(".%s",complianceOutputExtension);
				}
			case PUBLICITY_PROXY:
				{
					return String.format(".%s",publicityOutputExtension);
				}
			case TX:
				throw new IllegalArgumentException("Unexpected job type");
			default:
				throw new IllegalArgumentException("Unexpected job type");

		}
	}

	private int getPriority(Date firstTx, TranscodeJobType jobType)
	{
		Integer priority = transcodePriorities.getPriorityForNewTranscodeJob(jobType, firstTx).intValue();
		log.debug("priority is " + priority.intValue());
		return priority.intValue();
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
