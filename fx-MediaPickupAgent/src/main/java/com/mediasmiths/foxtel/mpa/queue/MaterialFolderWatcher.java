package com.mediasmiths.foxtel.mpa.queue;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MXF_NOT_TOUCHED_PERIOD;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.XML_NOT_TOUCHED_PERIOD;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;

public class MaterialFolderWatcher extends DirectoryWatchingQueuer
{

	private static Logger logger = Logger.getLogger(MaterialFolderWatcher.class);

	protected final RuzzFilesPendingProcessingQueue ruzzQueue;
	protected final WatchFolders watchFolders;

	@Inject
	public MaterialFolderWatcher(
			FilesPendingProcessingQueue filePathsPendingValidation,
			RuzzFilesPendingProcessingQueue ruzzfilePathsPendingProcesing,
			@Named("watchfolder.locations") WatchFolders paths,
			@Named(MXF_NOT_TOUCHED_PERIOD) Long mxfNotTouchedPeriod,
			@Named(XML_NOT_TOUCHED_PERIOD) Long xmlNotTouchedPeriod)
	{
		super(filePathsPendingValidation, paths);

		logger.info("setting not touched periods mxf:" + mxfNotTouchedPeriod + " xml:" + xmlNotTouchedPeriod);
		setNotTouchedPeriodForFormat("mxf", mxfNotTouchedPeriod);
		setNotTouchedPeriodForFormat("xml", xmlNotTouchedPeriod);

		this.ruzzQueue = ruzzfilePathsPendingProcesing;
		this.watchFolders = paths;
	}

	@Override
	public void onFileArrival(Path path)
	{
		File file = path.toFile();

		logger.debug(String.format("A file %s has arrived", file.getAbsolutePath()));

		if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".xml"))
		{
			logger.info(String.format("An xml file %s has arrived", file.getAbsolutePath()));
			queueFile(file);
		}

		if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".mxf"))
		{
			logger.info(String.format("A mxf file %s has arrived", file.getAbsolutePath()));
			queueFile(file);
		}
	}

	@Override
	protected IOFileFilter getExistingFilesFilter()
	{
		return acceptXMLorMXFFilesFilter;
	}

	private static IOFileFilter acceptXMLorMXFFilesFilter = new IOFileFilter()
	{

		@Override
		public boolean accept(File dir, String name)
		{
			return accept(name);
		}

		@Override
		public boolean accept(File file)
		{
			return accept(file.getName());
		}

		protected boolean accept(String name)
		{
			String extension = FilenameUtils.getExtension(name).toLowerCase(Locale.ENGLISH);
			return extension.equals("xml") || extension.equals("mxf");
		}

	};

	@Override
	protected boolean queueFile(File file)
	{

		String path = FilenameUtils.getPath(file.getAbsolutePath());
		logger.debug("path is" + path);

		if (watchFolders.isRuzz(path))
		{
			logger.debug("path is ruzz" + path);
			return ruzzQueue.add(file.getAbsolutePath());
		}
		else
		{
			logger.debug("path is not ruzz" + path);
			return this.filePathsPendingValidation.add(file.getAbsolutePath());
		}
	}
}
