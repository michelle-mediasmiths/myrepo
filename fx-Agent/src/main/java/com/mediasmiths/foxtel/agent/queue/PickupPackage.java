package com.mediasmiths.foxtel.agent.queue;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The data for items that have been picked up
 */
public class PickupPackage
{

	private final static Logger log = Logger.getLogger(PickupPackage.class);

	/*
	  Internally - all suffixes are lower case.
	 */
    public final Set<String> suffixes = new HashSet<String>();
	private Map<String, File> files = new HashMap<String, File>();
	//records when files were first added to pickup packages, must be removed later after processing or memory will leak!
	private static Map<String,Long> discoveredTimes = new HashMap<String,Long>();
	private Map<String,Long> modifiedTimes= new HashMap<String, Long>();

	private boolean isSuspectPickup = false;

	private String suspectReason;

	private String rootPath = null;
	private String rootName = null;


	public PickupPackage(String... suffixes)
	{
		 if (suffixes == null || suffixes.length == 0)
			 throw new IllegalArgumentException("No suffixes designated for a pickup collection.");

		 for (String e: suffixes)
		 {
			 if (e == null || e.length() == 0)
				 throw new IllegalArgumentException("Empty extension is not permitted - fatal");

			 this.suffixes.add(e.toLowerCase());
		 }

	}

	/**
	 *
	 * @return true if this pickup packet has been marked as suspect.
	 */
	public boolean isSuspectPickup()
	{
		return this.isSuspectPickup;
	}

	/**
	 * set this pick up as being suspicious and add a comment that explains why.
	 *
	 * @param comment a description as to why this pick up is suspect.
	 */
	public void setIsSuspectPickup(String comment)
	{
		this.isSuspectPickup = true;
		this.suspectReason = comment;
	}

	public String getSuspectReason()
	{
		return this.suspectReason;
	}


	public String getNameForExtension(String ext)
	{
		if (ext == null || !suffixes.contains(ext.toLowerCase()))
			throw new IllegalArgumentException("Not a known suffix for this package: " + ext);

	     return getRootName() + "." + ext;
	}

	public String getRootPath()
	{
		return rootPath;
	}

	public String getRootName()
	{
		return rootName;
	}


	public int expectedPickUpCount()
	{
	     return suffixes.size();
	}

	public int count()
	{
		return files.size();
	}

	public boolean isComplete()
	{
		return count() == expectedPickUpCount();
	}

	public boolean isExpectedSuffix(String suffix)
	{
		if (suffix == null)
			throw new IllegalArgumentException("Null suffix not permitted");

       return this.suffixes.contains(suffix.toLowerCase());
	}

	public boolean isPickedUpSuffix(String suffix)
	{
       return files.keySet().contains(suffix.toLowerCase());
	}

	public void addPickUp(String path)
	{
       if (path == null)
	       throw new IllegalArgumentException("Pick up path cannot be null");

		addPickUp(new File(path));

	}

	public void addPickUp(File path)
	{
        if (path == null)
	        throw new IllegalArgumentException("Pick up path cannot be null");

		String fName = path.getAbsolutePath();
		String ext = FilenameUtils.getExtension(fName).toLowerCase();
		if (ext == null || ext.length() == 0)
			throw new IllegalArgumentException("Empty extension - not permitted");

		if (!suffixes.contains(ext))
		{
			if (suffixes.contains(ext.toLowerCase()))
				ext = ext.toLowerCase();
		    else
			    throw new IllegalArgumentException("Not a recognised suffix for this pickup");
		}

		String cRootPath = FilenameUtils.getFullPathNoEndSeparator(fName);
		String cRootName = FilenameUtils.getBaseName(fName);
		if (rootPath == null)
		{ // nothing currently set
			rootPath = cRootPath;
			rootName = cRootName;
		}
		else
		{
			if (!cRootName.equals(rootName) || !cRootPath.equals(rootPath))
				throw new IllegalArgumentException("Root Path mismatch. Expected: " + rootPath + ":" + rootName
				                                   + " trying to add " + cRootPath + ":" + cRootName);
		}
		this.files.put(ext.toLowerCase(), path);
		addDiscoveryTime(path.getAbsolutePath(), System.currentTimeMillis());
		this.modifiedTimes.put(ext.toLowerCase(), path.lastModified());
	}


	public File getPickUp(String extension)
	{
		if (extension == null)
			throw new IllegalArgumentException("Empty extension");

		return this.files.get(extension.toLowerCase());
	}


	public Long getLastModified(String extension)
	{
		if (extension == null)
			throw new IllegalArgumentException("Empty extension");

		return this.modifiedTimes.get(extension.toLowerCase());
	}


	public Collection<File> getAllFiles(){
		return this.files.values();
	}

	/**
	 *
	 * @return the collection of suffixes that have been found.
	 */
	public Set<String> getFoundSuffixes()
	{
		return files.keySet();
	}



	/**
	 * Records the time the file at a given path was 'discovered'/considered stable for processing. Used to report time from stability to process finishing
	 * @param path
	 * @param time
	 */
	private static void addDiscoveryTime(String path, Long time)
	{

		if (!discoveredTimes.containsKey(path))//first time this method has been called for this path
		{
			discoveredTimes.put(path, time);
		}
	}


	/**
	 * Returns the time that the path requested was discovered, also removes the record of discovery so this method should only be called once for a given file path
	 * @param ext
	 * @return
	 */
	public Long pullTimeDiscovered(String ext)
	{

		File f = getPickUp(ext);
		String filePath = f.getAbsolutePath();

		if (discoveredTimes.containsKey(filePath))
		{
			Long ret = discoveredTimes.get(filePath);
			discoveredTimes.remove(filePath);
			return ret;
		}
		else
		{
			log.error("No discovered time for " + filePath + " falling back to last modified time");
			return getLastModified(ext);
		}
	}


	/**
	 * Clears the structure recording the times that files are discovered, as files appearing in drop folders and being deleted
	 * before they were processed has the potential to cause memory leaks this method should be called when it is known that there
	 * are no files waiting to be processed
	 */
	public static void clearDiscoveredTimes()
	{
		if (discoveredTimes.size() > 0)
		{
			log.debug(String.format("Clearing %d discovered times",discoveredTimes.size()));
			discoveredTimes.clear();
		}
	}
}
