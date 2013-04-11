package com.mediasmiths.foxtel.agent.queue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickUpKinds;
import com.mediasmiths.foxtel.ip.common.events.FilePickup;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.std.io.filter.FilenameExtensionFilter;

/**
 * An implementation of the FilePickUpProcessingQueue where the directory structure is used as the processing queue and the ordering
 * is provided by the lastModified dates of files;
 *
 * The pick up agent can be configured to pick up only files with a defined extension.
 *
 * File that are suspect are marked as suspect.
 *
 * This implementation permits Active and Passive behaviour where
 *
 * A client sets whether the service is active or passive using the setActive operation
 *
 * An active request can be set to be permanent.
 *
 *     HEARTBEAT_ENABLED == FALSE defines that a single active request enables pick up permanently
 *     HEARTBEAT_ENABLED == TRUE defines that repeated setActive requests are required and that an setActive request will expire
 *     after HEARTBEAT_WINDOW milliseconds.
 *
 */
public class SingleFilePickUp implements IFilePickup
{


	private Logger logger = Logger.getLogger(SingleFilePickUp.class);

	/** true if heartbeating is enabled and false otherwise */
	@Named("filepickup.heartbeat_enabled")
	boolean HEARTBEAT_ENABLED;

	/** the time in milliseconds that an setActive request is valid. */
	@Named("filepickup.heartbeat_window")
	long HEARTBEAT_WINDOW;


	/** the time in milliseconds that a file must not grow before being passed for processing */
	@Inject
	@Named("filepickup.file.stability_time")
	long STABILITY_TIME;

	/** The time in milliseconds that an active agent should sleep between file arrive check intervals */
	@Inject
	@Named("filepickup.sleep_time")
	long WAIT_TIME;

	/** The time in milliseconds that a passive agent should sleep before recheckings its status */
	@Inject
	@Named("filepickup.passive_sleep_time")
	long RESILIENCE_WAIT_TIME;

	/** The collection of directories that are being watched  */
	private final File[] pickUpDirectories;

	/** The kind of pick up that is being performed e.g. placeholder, media etc */
	@Inject
	@Named("filepickup.event.kind")
	FilePickUpKinds pickUpKind;

	/** the event sending agent for events if they are defined */
	@Inject
	EventService pickUpEventTimer;

	/** whether events are being recorded for pickup */
	@Inject
	@Named("filepickup.events.enabled")
	boolean eventsEnabled;

	/** the namespace to use for file pickup events */
	@Inject
	@Named("filepickup.event.namespace")
	String eventNamespace;

	/** the event name used for file pickup events */
	@Inject
	@Named("agent.events.pickupTimingType")
	String eventName;


	@Inject
	@Named("filepickup.watched.directories.max_file_size_bytes")
	long MAX_FILE_SIZE;

	/** true if agent is active and false if passive */
	private boolean isActive = true;

	/** the time in millisconds when the last setActive request was made */
	private long activationTime = 0;

	/** false if enclosing system is active and false if shutting down */
	private volatile boolean shuttingDown = false;

	/** is null or evaluates whether a filename has a valid extension */
	private final FilenameExtensionFilter filenameExtensionFilter;

	private final String validExtension;

	@Inject
	/**
	 * The files to be monitors are inject on construction to allow extending classes to pick up their directories from a different @Named parameter
	 * @param pickupDirectories File object referencing the pick up areas
	 * @param extensions the collection of extensions (strings) that are valid for files.
	 */
	public SingleFilePickUp(@Named("filepickup.watched.directories") File[] pickupDirectories, @Named("filepickup.single.extension") String extension)
	{

		if (extension == null)
			throw new IllegalArgumentException("File name extensions cannot be null or zero size");

		validExtension = extension;

		this.pickUpDirectories = pickupDirectories;

		filenameExtensionFilter = new FilenameExtensionFilter(false, extension);

		for (File f : pickUpDirectories)
		{
			logger.info("Watching: " + f.getAbsolutePath());
		}
	}

	/**
	 * set that the system is shutting down and activities should stop
	 */
	public void shutdown()
	{
		this.shuttingDown = true;
	}

	public void setState(final boolean isActive)
	{
		this.isActive = isActive;
		this.activationTime = System.currentTimeMillis();
		if (logger.isTraceEnabled())
			logger.trace("isActive: " + isActive + " @ " + activationTime);
	}

	public PickupPackage take()
	{

		do
		{
			try
			{
				waitUntilActive();

				if (!shuttingDown)
				{
					PickupPackage candidate = getOldestCandidate();

					if (candidate == null)
					{
						sleepUntilNextInspection();
					}
					else
					{
						return candidate;
					}
				}
			}
			catch (Throwable e)
			{
				logger.error("Pick up Agent has a problem. Pausing before trying to recover.",e);

				handle_processing_exception();
			}
		}
		while (!shuttingDown);

		logger.info("Shutdown down TAKE operation");
		return null;
	}


	// Private methods

	/**
	 *
	 * @return the oldest candate pickup or null if there is none from the watched directory set.
	 *
	 */
	private PickupPackage getOldestCandidate()
	{

		File[] candidateFiles = getTimeOrderedPendingFiles();

		if (candidateFiles == null || candidateFiles.length == 0)
			return null;

		for (File f: candidateFiles)
		{
			if (isStableFile(f))
			{

				PickupPackage pickup = getPickUpPackageForFile(f);


			    return pickup;

			}
		}

		return null;

	}



	/**
	 *
	 * @param f a stable file within a pick up directory
	 * @return the package of files that exist and are stable that are related to f that form the pickup collection
	 */
	private PickupPackage getPickUpPackageForFile(final File f)
	{
		PickupPackage pickUp = new PickupPackage(validExtension);

		pickUp.addPickUp(f);

		if (isSuspectFile(f))
		{
			pickUp.setIsSuspectPickup("File " + f.getAbsolutePath() + " is suspect because of its size and designated file tupe");
		}
		return pickUp;
	}

	/**
	 *
	 * @return the oldest file list in the collection of watched directories
	 */
	private File[] getTimeOrderedPendingFiles()
	{
		File[] candidates = null;

		for (File dir : pickUpDirectories)
		{
			File[] possibleCandidate = getTimeOrderedFilesInDirectory(dir);

			if (possibleCandidate != null)
			{
				candidates = mergeOrderedFileList(candidates, possibleCandidate);
			}
		}

		return candidates;

	}


	/**
	 *
	 * @param dir a watched directory
	 *
	 * @return the file that is the oldest (ie oldest unchanged) in a directory dir
	 */
	private File[] getTimeOrderedFilesInDirectory(File dir)
	{
		File[] files = dir.listFiles(filesWithValidExtensionOnly);

		if (files != null && files.length > 0)
		{
			Arrays.sort(files,oldestFileComparator);

			return files;
		}
		else
		{
			return null;
		}

	}

	/**
	 *
	 * @param l1 null or date ordered list of files
	 * @param l2 null or date ordered list of files
	 * @return composite date ordered list of files
	 */
	private File[] mergeOrderedFileList(final File[] l1, final File[] l2)
	{
		if (l1 == null || l1.length == 0)
			return l2;

		if (l2 == null || l2.length == 0)
			return l1;

		File[] res = new File[l1.length+l2.length];

		int i=0, j=0;
		int k=0;

		while (i < l1.length && j < l2.length)
		{
			if (oldestFileComparator.compare(l1[i],l2[j]) <= 0)
			{
				res[k] = l1[i];
				i++;
			}
			else
			{
				res[k] = l2[j];
				j++;
			}
			k++;
		}

		while (i < l1.length)
		{
			res[k] = l1[i];
			i++;
			k++;
		}

		while (j < l2.length)
		{
			res[k] = l2[j];
			j++;
			k++;
		}

		return res;

	}

	/**
	 *
	 * @param f candidate file
	 * @return true if the candidate file has been stable (unchanged) for STABILITY_TIME
	 */
	private boolean isStableFile(File f)
	{

		long now = System.currentTimeMillis();
		long then = f.lastModified();

		if(logger.isDebugEnabled()) logger.debug(String.format("now %d then %d STABILITY_TIME %d",now,then,STABILITY_TIME));

		return (System.currentTimeMillis() - f.lastModified()) >= STABILITY_TIME;
	}


	private Comparator<File> oldestFileComparator = new Comparator<File>()
	{
		@Override
		public int compare(File o1, File o2)
		{

			final Long aT = o1.lastModified();
			final long bT = o2.lastModified();

			return aT.compareTo(bT);

		}
	};


	private FilenameFilter filesWithValidExtensionOnly = new FilenameFilter()
	{

		@Override
		public boolean accept(final File dir, final String name)
		{
			File aFile = new File(dir, name);

			if (filenameExtensionFilter == null)
				return aFile.isFile();
			else
				return aFile.isFile() && filenameExtensionFilter.accept(dir, name);
		}
	};



	/**
	 * Wait until this watcher agent can pick up a file
	 */
	private void waitUntilActive()
	{
		while (isDisabled())
		{
			if (logger.isTraceEnabled()) logger.trace("IsActive: " + isActive + "sleep @" + System.currentTimeMillis());
			try
			{
				Thread.sleep(RESILIENCE_WAIT_TIME);
			}
			catch (InterruptedException e)
			{
				logger.info("Sleeping file pick up queue was interrupted - this has no effect");
			}
		}
	}

	/**
	 * Sleep for a period before looking for new files.
	 */
	private void sleepUntilNextInspection()
	{
		try
		{
			Thread.sleep(WAIT_TIME);
		}
		catch (InterruptedException e)
		{
			logger.info("Sleeping file pick up queue was interrupted - this has no effect");
		}
	}


	/**
	 * Defines whether pick up is currently enabled or disabled.
	 *
	 * @return true if the pick up is currently not enabled ie if isActive == false || isStaleActivation())
	 */
	private boolean isDisabled()
	{
		return !shuttingDown && (!isActive || isStaleActivation());
	}

	/**
	 *
	 * @return True if the activation is stale ie that a heartbeat was not sent within HEARTBEAT_WINDOW
	 */
	private boolean isStaleActivation()
	{
		return HEARTBEAT_ENABLED && (System.currentTimeMillis() - activationTime) > HEARTBEAT_WINDOW;
	}


	/**
	 *
	 * @param fileRef a file reference for an existing file that whose timings should be reported to the event system.
	 */
	private void sendPickUpTimingEvent(final File fileRef)
	{

		FilePickup pickUpStats = new FilePickup();
		pickUpStats.setPickUpKind(pickUpKind);
		pickUpStats.setFilePath(fileRef.getAbsolutePath());
		pickUpStats.setWaitTime(System.currentTimeMillis() -  fileRef.lastModified());

		if (eventsEnabled) pickUpEventTimer.saveEvent(eventName,pickUpStats,eventNamespace);
	}

	public File[] getWatchedDirectories()
	{
		return pickUpDirectories;
	}




	/**
	 * A processing exception has occurred. Sleep for a while and then start pick up again.
	 */
	private void handle_processing_exception()
	{
		try
		{
			Thread.sleep(WAIT_TIME);
		}
		catch (InterruptedException e)
		{
			logger.info("Sleeping thread woken - sleeping giving system time to recover");
		}
	}



	/**
	 *
	 * @param candidate a file that is the next file to be picked up
	 * @return true if this file is suspect and should be ignored.
	 */
	private boolean isSuspectFile(final File candidate)
	{
		return fileExceedsPickUpLimit(candidate);
	}

	/**
	 *
	 * @param candidate a file that is the next file to be picked up
	 * @return true if this file exceeds the file size limit if one has been defined.
	 */
	private boolean fileExceedsPickUpLimit(File candidate)
	{

		return MAX_FILE_SIZE != -1 && candidate.length() > MAX_FILE_SIZE;
	}



}
