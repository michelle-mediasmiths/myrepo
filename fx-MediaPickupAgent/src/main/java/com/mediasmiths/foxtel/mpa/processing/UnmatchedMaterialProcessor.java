package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.Util;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;

public class UnmatchedMaterialProcessor extends Daemon implements StoppableService {

	private final Long timeout;
	private final MatchMaker matchMaker;
	private final long sleepTime;
	private final WatchFolders watchFolders;

	private final EventService events;

	private static Logger logger = Logger
			.getLogger(UnmatchedMaterialProcessor.class);

	@Inject
	private MayamClient mayamClient;
	
	@Inject
	public UnmatchedMaterialProcessor(
			@Named(MEDIA_COMPANION_TIMEOUT) Long timeout,
			@Named(UNMATCHED_MATERIAL_TIME_BETWEEN_PURGES) Long sleepTime,
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			MatchMaker matchMaker, EventService events) {
		this.timeout = timeout;
		this.matchMaker = matchMaker;
		this.watchFolders = watchFolders;
		this.sleepTime = sleepTime.longValue();
		this.events = events;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {

			try {
				logger.trace("going to sleep");
				Thread.sleep(sleepTime);
				logger.trace("woke up");
				process();
			} catch (InterruptedException e) {
				logger.info("Interrupted", e);
				return;
			} catch (Exception e){
				logger.fatal("Something went badly wrong in UnmatchedMaterialProcessor.process()",e);
			}
		}
	}

	protected void process() {

		processUnmatchedMessages();
		processUnmatchedMXFs();

	}

	private void processUnmatchedMXFs()
	{
		Collection<UnmatchedFile> unmatchedMXFs = matchMaker.purgeUnmatchedMXFs(timeout.longValue());

		if (unmatchedMXFs.size() > 0)
		{
			logger.info(String.format("Found %d unmatched material mxfs", unmatchedMXFs.size()));
		}
		else
		{
			logger.debug(String.format("Found %d unmatched material mxfs", unmatchedMXFs.size()));
		}

		for (UnmatchedFile mxf : unmatchedMXFs)
		{
			logger.info(String.format("no material message found for %s", mxf.getFilePath()));
			/*
			 * 2.1.2.2 Media file is delivered without the companion XML file (or with a corrupt XML file) If a media file is delivered without the companion XML file (file has been not been modified
			 * in a configurable amount of time without the XML file appearing or is the XML file is not readable), the WFE shall move the media file to the emergency programme file auto import
			 * location described in section 2.3 “Emergency programme file ingest”.
			 */

			
			String sourceFolder = FilenameUtils.getFullPathNoEndSeparator(mxf.getFilePath());
			
			String destinationFolder;
			
			if(watchFolders.isAo(sourceFolder)){
				destinationFolder = MessageProcessor.getFailureFolderForFile(new File(mxf.getFilePath()));
			}
			else{
				destinationFolder = watchFolders.destinationFor(FilenameUtils.getFullPathNoEndSeparator(mxf.getFilePath()));
			}
			
			
			try
			{

				StringBuilder sb = new StringBuilder(destinationFolder);
				sb.append(IOUtils.DIR_SEPARATOR);
				sb.append(FilenameUtils.getName(mxf.getFilePath()));

				String destination = sb.toString();

				File dest = new File(destination);

				int i = 1;

				while (dest.exists())
				{
					logger.warn(String.format("Destination file %s already exists", destination));

					sb = new StringBuilder(destinationFolder);
					sb.append(IOUtils.DIR_SEPARATOR);
					sb.append(FilenameUtils.getBaseName(mxf.getFilePath()));
					sb.append("_");
					sb.append(i);
					sb.append(FilenameUtils.EXTENSION_SEPARATOR);
					sb.append(FilenameUtils.getExtension(mxf.getFilePath()));
					destination = sb.toString();
					dest = new File(destination);
					i++;
				}

				logger.info(String.format("Trying to move file %s to %s", mxf.getFilePath(), destination));
				FileUtils.moveFile(new File(mxf.getFilePath()), dest);
				
				String path = FilenameUtils.separatorsToUnix(FilenameUtils.getPathNoEndSeparator(mxf.getFilePath()));
				String aggregator = path.substring(path.lastIndexOf("/"));
				mayamClient.createWFEErrorTaskForUnmatched(aggregator, FilenameUtils.getBaseName(mxf.getFilePath()));
				
				//send event
				events.saveEvent("UnmatchedContentAvailable", destination);
				
			}
			catch (IOException e)
			{
				logger.error("IOException moving umatched mxf to emergency import folder", e);

				// send out alert that material could not be transferd to
				// emergency import folder
				StringBuilder sb = new StringBuilder();
				sb.append(String.format(
						"There has been a failure to deliver unmatched material %s to the Viz Ardome emergency import folder",
						FilenameUtils.getName(mxf.getFilePath())));
				events.saveEvent("error", sb.toString());
			}
			catch(Exception e){
				logger.error("Unhandled exception processing unmatched mxf",e);
			}
		}
	}

	private void processUnmatchedMessages() {
		Collection<MediaEnvelope> unmatchedMessages = matchMaker
				.purgeUnmatchedMessages(timeout.longValue());

		if (unmatchedMessages.size() > 0) {
			logger.info(String.format("Found %d unmatched material messages",
					unmatchedMessages.size()));
		} else {
			logger.debug(String.format("Found %d unmatched material messages",
					unmatchedMessages.size()));
		}

		for (MediaEnvelope me : unmatchedMessages) {
			logger.info(String.format("no mxf for %s", me.getFile()
					.getAbsolutePath()));

			boolean autoMatched = false;
			
			AutoMatchInfo ami = getSiteIDForAutomatch(me);
			
			if(ami != null){
				autoMatched = mayamClient.attemptAutoMatch(ami.siteID, FilenameUtils.getBaseName(ami.fileName));
			}
			
			if (autoMatched)
			{
				//move to completed folder
				try{
					String completedMessagesFolder = MessageProcessor.getArchivePathForFile(me.getFile().getAbsolutePath());
					File dst = new File(MessageProcessor.getDestinationPathForFileMove(me.getFile(), completedMessagesFolder, true));
					FileUtils.moveFile(me.getFile(), dst);
				}
				catch (IOException e)
				{
					logger.error("IOException moving umatched xml to the completed messages folder", e);
				}
			}
			else
			{
				// move message to failure folder
				try
				{
					String failedMessagesFolder = MessageProcessor.getFailureFolderForFile(me.getFile());
					File dst = new File(MessageProcessor.getDestinationPathForFileMove(me.getFile(), failedMessagesFolder, true));
					FileUtils.moveFile(me.getFile(), dst);
				}
				catch (IOException e)
				{
					logger.error("IOException moving umatched xml to the failed messages folder", e);
				}
			}
			

			// send out alert that no material arrived with this xml file
			StringBuilder sb = new StringBuilder();
			sb.append(String
					.format("There has been been no media received for Material message %s with MasterID %s ",
							FilenameUtils.getName(me.getFile()
									.getAbsolutePath()), me.getMasterID()));
			events.saveEvent("warning", sb.toString());
		}
	}

	class AutoMatchInfo{
		String siteID;
		String fileName;
	}
	
	private AutoMatchInfo getSiteIDForAutomatch(MediaEnvelope unmatchedMessage)
	{
		try
		{

			Object message = unmatchedMessage.getMessage();
			if (message instanceof Material)
			{

				if (Util.isProgramme((Material) message))
				{

					AutoMatchInfo ret = new AutoMatchInfo();
					ret.siteID = ((Material) message).getTitle().getProgrammeMaterial().getMaterialID();
					ret.fileName = ((FileMediaType) ((Material) message).getTitle().getProgrammeMaterial().getMedia()).getFilename();
					logger.debug("attempt to automatch on filename from programme material xml");
					return ret;
				}
				else
				{
					logger.debug("cannot automatch marketing material");
					return null;
				}
			}
			else if (message instanceof RuzzIF)
			{

				AutoMatchInfo ret = new AutoMatchInfo();
				ret.siteID = ((RuzzIngestRecord) message).getMaterial().getMaterialID();
				ret.fileName = unmatchedMessage.getFile().getName();

				return ret;
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("error determining automatch info for message " + unmatchedMessage.getFile().getAbsolutePath());
			return null;
		}
	}

	@Override
	protected boolean shouldStartAsDaemon()
	{
		return true;
	}
	
	@Override
	public void shutdown()
	{
		stopThread();		
	}
}
