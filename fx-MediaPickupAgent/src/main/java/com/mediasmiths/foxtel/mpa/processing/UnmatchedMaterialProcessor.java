package com.mediasmiths.foxtel.mpa.processing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.MayamClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;

public class UnmatchedMaterialProcessor
{

	private final WatchFolders watchFolders;

	private final EventService events;

	private static Logger logger = Logger.getLogger(UnmatchedMaterialProcessor.class);

	@Inject
	private MayamClient mayamClient;

	public MayamClient getMayamClient()
	{
		return mayamClient;
	}

	public void setMayamClient(MayamClient mayamClient)
	{
		this.mayamClient = mayamClient;
	}

	@Inject
	public UnmatchedMaterialProcessor(
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders, 
			EventService events)
	{
		this.watchFolders = watchFolders;
		this.events = events;
	}

	public void processUnmatchedMXF(File mxf) throws IOException
	{

		logger.info(String.format("importing mxf as unmatched  %s", mxf.getAbsolutePath()));
		/*
		 * 2.1.2.2 Media file is delivered without the companion XML file (or with a corrupt XML file) If a media file is delivered without the companion XML file (file has been not been modified in a
		 * configurable amount of time without the XML file appearing or is the XML file is not readable), the WFE shall move the media file to the emergency programme file auto import location
		 * described in section 2.3 “Emergency programme file ingest”.
		 */

		String sourceFolder = FilenameUtils.getFullPathNoEndSeparator(mxf.getAbsolutePath());
		String destinationFolder = watchFolders.destinationFor(FilenameUtils.getFullPathNoEndSeparator(mxf.getAbsolutePath()));
		
		try
		{
			StringBuilder sb = new StringBuilder(destinationFolder);
			sb.append(IOUtils.DIR_SEPARATOR);
			sb.append(FilenameUtils.getName(mxf.getAbsolutePath()));

			String destination = sb.toString();

			File dest = new File(destination);

			int i = 1;

			while (dest.exists())
			{
				logger.warn(String.format("Destination file %s already exists", destination));

				sb = new StringBuilder(destinationFolder);
				sb.append(IOUtils.DIR_SEPARATOR);
				sb.append(FilenameUtils.getBaseName(mxf.getAbsolutePath()));
				sb.append("_");
				sb.append(i);
				sb.append(FilenameUtils.EXTENSION_SEPARATOR);
				sb.append(FilenameUtils.getExtension(mxf.getAbsolutePath()));
				destination = sb.toString();
				dest = new File(destination);
				i++;
			}

			logger.info(String.format("Trying to move file %s to %s", mxf.getAbsolutePath(), destination));
			FileUtils.moveFile(mxf, dest);
		}
		catch (IOException e)
		{
			logger.error("IOException moving umatched mxf to import folder", e);

			// send out alert that material could not be transferd to
			// emergency import folder
			StringBuilder sb = new StringBuilder();
			sb.append(String.format(
					"There has been a failure to deliver unmatched material %s to the Viz Ardome import folder",
					FilenameUtils.getName(mxf.getAbsolutePath())));
			events.saveEvent("error", sb.toString());
			throw e;
		}		
	}

}
