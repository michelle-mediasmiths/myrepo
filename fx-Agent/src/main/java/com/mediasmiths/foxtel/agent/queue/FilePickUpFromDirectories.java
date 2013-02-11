package com.mediasmiths.foxtel.agent.queue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickUpKinds;
import com.mediasmiths.foxtel.ip.common.events.FilePickup;
import com.mediasmiths.foxtel.ip.event.EventService;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * An implementation of the FilePickUpProcessingQueue where the directory structure is used as the processing queue and the ordering
 * is provided by the lastModified dates of files;
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
public class FilePickUpFromDirectories implements FilePickUpProcessingQueue
{

	private Logger logger = Logger.getLogger(FilePickUpFromDirectories.class);

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
	@Inject
	@Named("filepickup.watched.directories")
	private File[] pickUpDirectories;

	/** The kind of pick up that is being performed e.g. placeholder, media etc */
    @Inject
    @Named("filepickup.event.kind")
	FilePickUpKinds pickUpKind;

	/** the event sending agent for events if they are defined */
	@Inject
	EventService pickUpEventTimer;

	/** whether events are being recorded for pickup */
	@Inject
	@Named("service.events.enabled")
	boolean eventsEnabled;
	
	/** the namespace to use for file pickup events */
	@Inject
	@Named("filepickup.event.namespace")
	String eventNamespace;
	
	/** the event name used for file pickup events */
	@Inject
	@Named("agent.events.pickupTimingType")
	String eventName;

    /** true if agent is active and false if passive */
	private boolean isActive = true;

	/** the time in millisconds when the last setActive request was made */
	private long activationTime = 0;

	/** false if enclosing system is active and false if shutting down */
    private volatile boolean shuttingDown = false;

	/**
	 * set that the system is shutting down and activities should stop
	 */
    @Override
	public void shutdown()
	{
		this.shuttingDown = true;
	}

	@Override
	public void setState(final boolean isActive)
	{
		this.isActive = isActive;
		this.activationTime = System.currentTimeMillis();
		if (logger.isTraceEnabled())
			logger.trace("isActive: " + isActive + " @ " + activationTime);
 	}

	@Override
	public File take()
	{

		do
		{
			waitUntilActive();

			if (!shuttingDown)
			{
				File candidate = getOldestPendingFile();

				if (candidate != null && isStableFile(candidate))
				{
					sendPickUpTimingEvent(candidate);
					return candidate;
				}
				else
				{
					sleepUntilFileStable();
				}
			}
		}
		while (!shuttingDown);

		logger.info("Shutdown occurring");
		return null;
	}

	@Override
	public void add(final String filePath)
	{
		throw new RuntimeException("Call to add path - " + filePath + " implementation does not support this operation");
	}


	@Override
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

	@Override
	public long size()
	{
		long files=0;
		for (File d: pickUpDirectories)
		{
			files += d.list().length;
		}
		return files;
	}

	@Override
	public void clear()
	{
		logger.info("Clear is currently not supported by a directory");
	}


	// Private methods



	/**
	 *
	 * @return the oldest file in the collection of watched directories
	 */
	private File getOldestPendingFile()
	{
		File candidate;

		candidate = getOldestFileInDirectory(pickUpDirectories[0]);

		for (int i = 1; i < pickUpDirectories.length; i++)
		{
			File posCandidate = getOldestFileInDirectory(pickUpDirectories[i]);

			if (posCandidate != null && oldestFileComparator.compare(posCandidate, candidate) < 0)
			{
				candidate = posCandidate;
			}
		}
		return candidate;
	}



	/**
	 *
	 * @param dir a watched directory
	 *
	 * @return the file that is the oldest (ie oldest unchanged) in a directory dir
	 */
	private File getOldestFileInDirectory(File dir)
	{
		File[] files = dir.listFiles(filesOnly);

		if (files != null && files.length > 0)
		{
			Arrays.sort(files,oldestFileComparator);

			return files[0];
		}
		else
		{
			return null;
		}

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


	private FilenameFilter filesOnly = new FilenameFilter()
	{
		@Override
		public boolean accept(final File dir, final String name)
		{
			return (new File(dir, name)).isFile();
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
	 * Sleep for a defined period to enable a file to become stable
	 */
	private void sleepUntilFileStable()
	{
		try
		{
			if (!shuttingDown)
				Thread.sleep(WAIT_TIME);
		}
		catch (InterruptedException e)
		{
			logger.info("Sleeping file watcher woken by an interrupt - ignored");
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



}
