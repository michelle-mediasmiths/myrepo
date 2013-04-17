package com.mediasmiths.foxtel.agent.queue;

import org.apache.commons.io.FilenameUtils;

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

	/*
	  Internally - all suffixes are lower case.
	 */
    public final Set<String> suffixes = new HashSet<String>();
	private Map<String, File> files = new HashMap<String, File>();

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

	}

	public File getPickUp(String extension)
	{
		if (extension == null)
			throw new IllegalArgumentException("Empty extension");

		return this.files.get(extension.toLowerCase());
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

}
