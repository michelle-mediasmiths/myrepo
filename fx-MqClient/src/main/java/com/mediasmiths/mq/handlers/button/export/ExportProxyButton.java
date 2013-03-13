package com.mediasmiths.mq.handlers.button.export;


import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
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
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.button.ButtonClickHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public abstract class ExportProxyButton extends ButtonClickHandler
{
	private final static Logger log = Logger.getLogger(ExportProxyButton.class);

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
	@Named("export.transient.tc.output.location") //where transcoded files go before ftp upload + deletion
	private String exportOutputLocation;
	
	@Inject
	protected ChannelProperties channelProperties;
	
	@Override
	protected void buttonClicked(AttributeMap materialAttributes)
	{
		boolean isAO = AssetProperties.isAO(materialAttributes);

		if (!isAO)
		{

			String outputFileName = (String) materialAttributes.getAttribute(Attribute.OP_FILENAME);
			String buglocation = (String) materialAttributes.getAttribute(Attribute.VISUAL_BUG);
			String timecodePosition = (String) materialAttributes.getAttribute(Attribute.VISUAL_TIMECODE_POSITION);
			String timecodeColour = (String) materialAttributes.getAttribute(Attribute.VISUAL_TIMECODE_COLOR);
			StringList channels = materialAttributes.getAttribute(Attribute.CHANNELS);
			String materialID = (String) materialAttributes.getAttribute(Attribute.HOUSE_ID);
			boolean isSurround = AssetProperties.isMaterialSurround(materialAttributes);
			boolean isSD = AssetProperties.isMaterialSD(materialAttributes);
			String title = (String) materialAttributes.getAttribute(Attribute.ASSET_TITLE);
			Date firstTX = (Date) materialAttributes.getAttribute(Attribute.TX_FIRST);

			// create export task
			long taskID;
			try
			{
				taskID = createExportTask(materialID, materialAttributes,getJobType());
			}
			catch (MayamClientException e1)
			{
				log.error("error creating export task", e1);
				return;
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
						materialAttributes, taskID);
			}
			catch (MayamClientException e)
			{
				log.error("error constructing job params for export proxy", e);
				taskController.setTaskToErrorWithMessage(taskID, "Error constructing transcode paramters");
				return;
			}
			catch(Exception e){
				log.error("error constructing job params for export proxy", e);
				taskController.setTaskToErrorWithMessage(taskID, e.getMessage());
				return;
			}

			// invoke export flow
			try
			{
				initiateWorkflow(title, firstTX, materialID, jobParams, taskID);
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
			
			//set task to active state
			try
			{
				AttributeMap task;task = taskController.getTask(taskID);
				AttributeMap updateMap = taskController.updateMapForTask(task);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ACTIVE);
				taskController.saveTask(updateMap);
			}
			catch (RemoteException e)
			{
				log.error("error setting task "+ taskID + " to state ACTIVE",e);
			}
			catch (MayamClientException e)
			{
				log.error("error setting task "+ taskID + " to state ACTIVE",e);
			}
			
		}
		else{
			log.info("Item is ao, will not attempt to export");
		}

	}

	protected abstract String getJobType();

	private void initiateWorkflow(String assetTitle, Date firstTX, String materialID, TCJobParameters jobParams, long taskID)
			throws UnsupportedEncodingException,
			JAXBException, MuleException
	{
		InvokeExport ie = new InvokeExport();
		ie.setAssetID(materialID);
		ie.setRequiredDate(firstTX);
		ie.setTaskID(taskID);
		ie.setTcParams(jobParams);
		ie.setTitle(assetTitle);

		mule.initiateExportWorkflow(ie);
	}

	private long createExportTask(String materialID, AttributeMap materialAttributes, String jobType)
			throws MayamClientException
	{
		return taskController.createExportTask(materialID, materialAttributes,jobType);
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
			AttributeMap materialAttributes, long taskID) throws MayamClientException
	{
		TCJobParameters jobParams = new TCJobParameters();
		String channel=null;
		
		// get the first channel
		if (channels != null && channels.get(0) != null)
		{
			String firstChannel = channels.get(0);
			channel = firstChannel;
		}
		else{
			throw new IllegalArgumentException("no channels in asset metadata");
		}
		
		if (buglocation != null)
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

		String separatorAndExtension = getOutputFileExtension();
		
		if (outputFileName != null) 
		{
			jobParams.outputFileBasename = outputFileName + separatorAndExtension;
		}
		else
		{
			jobParams.outputFileBasename = materialID + separatorAndExtension;
		}

		jobParams.timecode = timecode(timecodeColour, timecodePosition);
		jobParams.purpose = getPurpose();
		
		Date materialFirstTX = materialAttributes.getAttribute(Attribute.TX_FIRST);
		
		if(materialFirstTX == null){
			log.warn("no first tx date set on material! will assume that first tx is > 7 days aways");
		}
		
		jobParams.priority = getPriority(materialFirstTX);

		jobParams.inputFile = mayamClient.pathToMaterial(materialID);
		jobParams.outputFolder = exportOutputLocation + "/" + taskID;
		
		jobParams.ftpupload = new TCFTPUpload();
		jobParams.ftpupload.filename=jobParams.outputFileBasename;
		jobParams.ftpupload.folder=getTranscodeDestination(materialAttributes);
		jobParams.ftpupload.user=exportFTPUser;
		jobParams.ftpupload.password=exportFTPPassword;
		jobParams.ftpupload.server=exportFTPServer;
				
		return jobParams;
	}

	/**
	 * returns a string to be added to the end of output file name, eg .mpg
	 * @return
	 */
	protected abstract String getOutputFileExtension();
	
	protected abstract String getTranscodeDestination(AttributeMap materialAttributes);

	protected abstract int getPriority(Date firstTx);

	protected abstract TCOutputPurpose getPurpose();

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

		throw new IllegalArgumentException("unrecognised bug locaiton");
	}
	
	protected String getExportLocationForFirstChannel(AttributeMap materialAttributes){
		StringList channels = materialAttributes.getAttribute(Attribute.CHANNELS);
		
		if(channels.size()==0){
			throw new IllegalArgumentException("no channels found for material, cannot pick export location");
		}
		
		String channelTag = channels.get(0);
		String channelGroup = channelProperties.channelGroupForChannel(channelTag);
		String exportLocation = channelProperties.exportPathForChannelGroup(channelGroup);
		return exportLocation;
		
	}

	protected final static long ONE_DAY = 1000l * 3600l * 24;
	protected final static long TWO_DAYS = ONE_DAY * 2;
	protected final static long THREE_DAYS = ONE_DAY * 3;
	protected final static long SEVEN_DAYS = ONE_DAY * 7;
	protected final static long EIGHT_DAYS = ONE_DAY * 8;
	
}
