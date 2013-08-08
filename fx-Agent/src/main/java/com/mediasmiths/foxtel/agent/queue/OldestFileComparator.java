package com.mediasmiths.foxtel.agent.queue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class OldestFileComparator implements Comparator<File>
{
	private Map<String, Long> modifiedTimeCache = new HashMap<String, Long>();


	@Override
	public synchronized int compare(File o1, File o2)
	{
		final Long aT = getLastModifedTime(o1);
		final long bT = getLastModifedTime(o2);

		return aT.compareTo(bT);
	}


	private Long getLastModifedTime(final File file)
	{
		final String path = file.getAbsolutePath();

		final Long cachedValue = modifiedTimeCache.get(path);
		
		if (cachedValue != null)
		{
			return cachedValue;
		}

		File f = new File(path);
		try
		{
			BasicFileAttributes basicFileAttributes = Files.getFileAttributeView(f.toPath(), BasicFileAttributeView.class)
			                                               .readAttributes();
			final Long lastModified = basicFileAttributes.lastModifiedTime().toMillis();

			modifiedTimeCache.put(path, lastModified);

			return lastModified;
		}
		catch (IOException e)
		{
			//error getting attributes using java.nio, fall back to java.io
			final Long lastModified = file.lastModified();
			modifiedTimeCache.put(path, lastModified);
			return lastModified;
		}
	}
}
