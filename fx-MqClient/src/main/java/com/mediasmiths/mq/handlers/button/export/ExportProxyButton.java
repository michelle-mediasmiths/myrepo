package com.mediasmiths.mq.handlers.button.export;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.DateUtil;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
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

	// @Inject
	// private MuleWorkflowController mule;

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
						materialAttributes);
			}
			catch (MayamClientException e)
			{
				log.error("error constructing job params for export proxy", e);
				return;
			}

			// create export task
			long taskID;
			try
			{
				taskID = createExportTask(jobParams, materialID, materialAttributes);
			}
			catch (MayamClientException e1)
			{
				log.error("error creating export task", e1);
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
			}
			catch (JAXBException e)
			{
				log.error("error initiating export workflow", e);
			}
		}
		else{
			log.info("Item is ao, will not attempt to export");
		}

	}

	private void initiateWorkflow(String assetTitle, Date firstTX, String materialID, TCJobParameters jobParams, long taskID)
			throws UnsupportedEncodingException,
			JAXBException
	{
		InvokeExport ie = new InvokeExport();
		ie.setAssetID(materialID);
		ie.setRequiredDate(firstTX);
		ie.setTaskID(taskID);
		ie.setTcParams(jobParams);
		ie.setTitle(assetTitle);

		// mule.initiateExportWorkflow(ie);
	}

	private long createExportTask(TCJobParameters jobParams, String materialID, AttributeMap materialAttributes)
			throws MayamClientException
	{
		return taskController.createExportTask(materialID, materialAttributes);
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
			AttributeMap materialAttributes) throws MayamClientException
	{
		TCJobParameters jobParams = new TCJobParameters();

		if (buglocation != null)
		{
			TCBugOptions bug = bug(buglocation, channels);
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

		if (outputFileName != null)
		{
			jobParams.outputFileBasename = outputFileName;
		}
		else
		{
			jobParams.outputFileBasename = materialID;
		}

		jobParams.timecode = timecode(timecodeColour, timecodePosition);
		jobParams.purpose = getPurpose();
		jobParams.priority = getPriority(materialAttributes);

		jobParams.inputFile = mayamClient.pathToMaterial(materialID);
		jobParams.outputFolder = getOutputFolder(materialAttributes);
		return jobParams;
	}

	protected abstract String getOutputFolder(AttributeMap materialAttributes);

	protected abstract int getPriority(AttributeMap materialAttributes);

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
		if (charAt == 'W')
		{
			return TCTimecodeColour.WHITE;
		}
		else if (charAt == 'B')
		{
			return TCTimecodeColour.BLACK;
		}
		else if (charAt == 'T')
		{
			return TCTimecodeColour.TRANSPARENT;
		}

		throw new IllegalArgumentException("Unknown timecode colour");
	}

	private TCBugOptions bug(String buglocation, StringList channels)
	{
		// a bug location has been specified
		TCBugOptions bug = new TCBugOptions();

		// get the first channel
		if (channels != null && channels.get(0) != null)
		{
			String firstChannel = channels.get(0);
			bug.channel = firstChannel;
			bug.position = location(buglocation);
		}
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

}
