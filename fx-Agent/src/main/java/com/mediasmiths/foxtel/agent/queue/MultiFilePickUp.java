package com.mediasmiths.foxtel.agent.queue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.FilePickUpKinds;
import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import com.mediasmiths.foxtel.ip.common.events.PickupNotification;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.std.io.filter.FilenameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the FilePickUpProcessingQueue where the directory structure is used as the processing queue and the ordering
 * is provided by the lastModified dates of files;
 *
 * This version picks up a collection of files with the same root file name but different extensions.
 *
 * Picks up that are suspect are marked as such.
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
public class MultiFilePickUp implements IFilePickup
{

	private Logger logger = Logger.getLogger(MultiFilePickUp.class);

	/** true if heartbeating is enabled and false otherwise */
	@Named("filepickup.heartbeat_enabled")
	boolean HEARTBEAT_ENABLED;

	/** the time in milliseconds that an setActive request is valid. */
	@Named("filepickup.heartbeat_window")
    long HEARTBEAT_WINDOW;

	/** the time in milliseconds that a file must not grow before being passed for processing */
	/** maps a folder location to a stability time */
	@Inject
	@Named("filepickup.watched.directories.stabilitytimes")
	protected Map<File,Long> stabilityTimes;

	/** the time in milliseconds that a file must not grow before being consider part of a timed out partial pickup */
	@Inject
	@Named("filepickup.file.partial_pickup_timeout_interval")
    long PICKUP_TIME_OUT_INTERVAL;

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


	/** the valid extension in a multi file pick up */
	private final String[] fileExtensions;


    @Inject
    /**
     * The files to be monitors are inject on construction to allow extending classes to pick up their directories from a different @Named parameter
     * @param pickupDirectories File object referencing the pick up areas
     * @param extensions the collection of extensions (strings) that are valid for files.
     */
    public MultiFilePickUp(File[] pickupDirectories, String... extensions)
    {

	    if (extensions == null || extensions.length == 0)
		    throw new IllegalArgumentException("File name extensions cannot be null or zero size");

	    this.pickUpDirectories = pickupDirectories;
	    this.fileExtensions = extensions;

	    filenameExtensionFilter = new FilenameExtensionFilter(false, extensions);

	    logger.info("Watching for packages with extensions: " + Arrays.toString(extensions));
	    System.out.println("Watching for packages with extensions: " + Arrays.toString(extensions));

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
						System.out.println("print - " + candidate.getRootName());

						sendPickUpTimingEvent(candidate);

						return candidate;
					}
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				logger.error("Pick up Agent has a problem. Pausing before trying to recover.",e);

				handle_processing_exception();
			}
		}
		while (!shuttingDown);

		logger.info("Shutdown down TAKE operation");
		return null;
	}

	public List<String> getPending()
	{
		List<String> pendingFiles = new ArrayList<>();
		for (File d: pickUpDirectories)
		{
			String[] l = d.list();
			if (l != null && l.length > 0)
				pendingFiles.addAll(Arrays.asList(l));
		}
		return pendingFiles;
	}

	public long size()
	{
		long files=0;
		for (File d: pickUpDirectories)
		{
			files += d.list().length;
		}
		return files;
	}

	public void clear()
	{
		logger.info("Clear is currently not supported by a directory");
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
			System.out.println("loop " + f.getAbsolutePath());
			if (isStableFile(f))
			{
				PickupPackage pickup = getPickUpPackageForFile(f);

				if (pickup.isComplete() || isTimedOutPickup(pickup))
				{
					return pickup;
				}
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

		PickupPackage pickUp = new PickupPackage(fileExtensions);

		pickUp.addPickUp(f);

		// fetch all of the files in f's directory that should be part of the pickup.
		for (String fPath : (new File(pickUp.getRootPath())).list(new RootNameFilenameFilter(pickUp.getRootName(), fileExtensions)))
		{
			String ext = FilenameUtils.getExtension(fPath);

			File cf = new File(pickUp.getRootPath(),fPath);

			if (!pickUp.isPickedUpSuffix(ext))
			{
				if (isStableFile(cf))
				{
					pickUp.addPickUp(cf);
				}
			}
			if (isSuspectFile(cf))
			{
				pickUp.setIsSuspectPickup("File " + cf.getAbsolutePath() + " is suspect because of its size and designated file tupe");
			}
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
	 * @param pickup a partial collection of files that is part of a pick up.  Decide if this pick up has timed out and should be returned.
	 * @return true if the collection of files have been stable long enough for this to be considered a timed out pickup.
	 *
	 */
	private boolean isTimedOutPickup(final PickupPackage pickup)
	{
		for (String ext: fileExtensions)
		{
			if (pickup.isPickedUpSuffix(ext))
			{
				final File f = pickup.getPickUp(ext);

				if (!isStableFile(f) || withinTimeoutPeriod(f))
					return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @param f a valid file that is a candidate for pickup
	 * @return true if the file has been stable long enough to be considered part of a timed out pick up...and false otherwise.
	 *
	 */
	private boolean withinTimeoutPeriod(final File f)
	{
		long now = System.currentTimeMillis();
		long then = f.lastModified();

		return now-then < PICKUP_TIME_OUT_INTERVAL;
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

		Long stabilityTime = stabilityTimes.get(f.getParentFile());
				
		if(logger.isDebugEnabled()) logger.debug(String.format("now %d then %d STABILITY_TIME %d",now,then,stabilityTime));

		return (System.currentTimeMillis() - f.lastModified()) >= stabilityTime;
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
	 * @param pp a pickup reference for an existing file that whose timings should be reported to the event system.
	 */
	private void sendPickUpTimingEvent(final PickupPackage pp)
	{

		PickupNotification pickUpStats = new PickupNotification();
		for (String ext : pp.getFoundSuffixes())
		{
			File f = pp.getPickUp(ext);
			FilePickupDetails pd = new FilePickupDetails();
			pd.setFilename(f.getName());
			pd.setFilePath(pp.getRootPath());
			pd.setTimeDiscovered(f.lastModified());
			pd.setTimeProcessed(System.currentTimeMillis());
			pickUpStats.getDetails().add(pd);

		}

		if (eventsEnabled) pickUpEventTimer.saveEvent("http://www.foxtel.com.au/ip/infrastructure",
		                                              EventNames.FILE_PICK_UP, pickUpStats);
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

	public void setStabilityTimes(Map<File, Long> stabilityTimes)
	{
		this.stabilityTimes = stabilityTimes;
	}

}

