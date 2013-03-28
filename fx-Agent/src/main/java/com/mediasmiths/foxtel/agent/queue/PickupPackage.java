package com.mediasmiths.foxtel.agent.queue;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The data for items that have been picked up
 */
public class PickupPackage
{

    private Set<FileExtensions> suffixes = new HashSet<FileExtensions>();
	private Map<FileExtensions, File> files;

	private String rootPath = null;
	private String rootName = null;

	public PickupPackage(FileExtensions... suffixes)
	{
		 if (suffixes == null || suffixes.length == 0)
			 throw new IllegalArgumentException("No suffixes designated for a pickup collection.");

		 this.files = new HashMap<FileExtensions, File>();

		 for (FileExtensions e: suffixes)
		 {
			 if (e != null)
			    this.suffixes.add(e);
		 }

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

	public boolean isExpectedSuffix(FileExtensions suffix)
	{
		if (suffix == null)
			throw new IllegalArgumentException("Null suffix no permitted");

       return this.suffixes.contains(suffix);
	}

	public boolean isPickedUpSuffix(FileExtensions suffix)
	{
       return files.keySet().contains(suffix);
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
		FileExtensions ext = FileExtensions.fromString(FilenameUtils.getExtension(fName));
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
		this.files.put(ext, path);

	}

	public File getPickUp(FileExtensions extension)
	{
		if (extension == null)
			throw new IllegalArgumentException("Empty extension");

		return this.files.get(extension);
	}

}
