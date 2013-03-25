package com.mediasmiths.foxtel.agent.queue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The data for items that have been picked up
 */
public class PickupPackage
{

	private List<String> suffixes;
	private Map<String, File> files;

	public PickupPackage(String... suffixes)
	{
		 if (suffixes == null || suffixes.length == 0)
			 throw new IllegalArgumentException("No suffixes designated for a pickup collection.");

         this.suffixes = new ArrayList<String>();
		 this.files = new HashMap<String, File>();
	}

	public int expectedPickUpCount()
	{
	     return suffixes.size()+1;
	}

	public int count()
	{
		return files.size()+1;
	}

	public boolean isComplete()
	{
		return count() == expectedPickUpCount();
	}

	public boolean isExpectedSuffix(String suffix)
	{
       return false;
	}

	public boolean isPickedUpSuffix(String suffix)
	{
       return false;
	}

	public void addPickUp(String path)
	{

	}

	public void addPickUp(File path)
	{

	}

}
