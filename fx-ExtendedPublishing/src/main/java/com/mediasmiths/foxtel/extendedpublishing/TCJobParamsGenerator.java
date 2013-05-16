package com.mediasmiths.foxtel.extendedpublishing;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
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
import com.mediasmiths.foxtel.transcode.TranscodeRules;

public class TCJobParamsGenerator
{
	private final static Logger log = Logger.getLogger(TCJobParamsGenerator.class);

	@Inject
	private TranscodePriorities transcodePriorities;

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
	OutputPaths outputPaths;
	
	@Inject
	TranscodeRules transcodeOutputRules;
	
	public TCJobParameters jobParams(
			boolean isSurround,
			boolean isSD,
			String outputFileName,
			String buglocation,
			String timecodePosition,
			String timecodeColour,
			String materialID,
			String channelTag,
			long taskID,
			Date firstTX,
			TranscodeJobType jobType,
			String inputFile)
	{
		TCJobParameters jobParams = new TCJobParameters();

		log.debug("buglocation: " + buglocation);
		if (buglocation != null && !buglocation.equals("--") && channelTag!=null)
		{
			TCBugOptions bug = bug(buglocation, channelTag);
			jobParams.bug = bug;
		}

		jobParams.audioType = transcodeOutputRules.audioTypeForTranscode(isSD, isSurround, isSD);

		if (isSD)
		{
			jobParams.resolution = TCResolution.SD;
		}
		else
		{
			jobParams.resolution = TCResolution.HD;
		}

		String separatorAndExtension = outputPaths.getOutputFileExtension(jobType);

		if (outputFileName != null)
		{
			jobParams.outputFileBasename = outputFileName + separatorAndExtension;
		}
		else
		{
			jobParams.outputFileBasename = materialID + separatorAndExtension;
		}

		jobParams.timecode = timecode(timecodeColour, timecodePosition, jobType);
		jobParams.purpose = getPurpose(jobType);

		jobParams.priority = getPriority(firstTX, jobType);

		jobParams.inputFile = inputFile;
		jobParams.outputFolder = exportOutputLocation + "/" + taskID;

		jobParams.ftpupload = new TCFTPUpload();
		jobParams.ftpupload.filename = jobParams.outputFileBasename;
		jobParams.ftpupload.folder = outputPaths.getFTPPathToExportDestinationFolder(channelTag, jobType);
		jobParams.ftpupload.user = exportFTPUser;
		jobParams.ftpupload.password = exportFTPPassword;
		jobParams.ftpupload.server = exportFTPServer;

		return jobParams;
	}

	public TCBugOptions bug(String buglocation, String channel)
	{
		// a bug location has been specified
		TCBugOptions bug = new TCBugOptions();
		bug.channel = channel;
		bug.position = location(buglocation);
		return bug;
	}

	public int getPriority(Date firstTx, TranscodeJobType jobType)
	{
		Integer priority = transcodePriorities.getPriorityForNewTranscodeJob(jobType, firstTx).intValue();
		log.debug("priority is " + priority.intValue());
		return priority.intValue();
	}

	public TCOutputPurpose getPurpose(TranscodeJobType jobType)
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

	public TCTimecodeOptions timecode(String timecodeColour, String timecodePosition, TranscodeJobType jobType)
	{

		//CR001 - Caption proxies
		//Timecode position must be at the top of the screen.
		//Timecode must be white text on a black background. 
		//The timecode font and font size will be fixed.
		if(jobType.equals(TranscodeJobType.CAPTION_PROXY)){
			timecodeColour="WoB";
			timecodePosition="Top";
		}

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

	public TCLocation timeCodeLocation(String timecodePosition)
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

	public TCTimecodeColour timeCodeColour(char charAt)
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

	public TCLocation location(String location)
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

}
