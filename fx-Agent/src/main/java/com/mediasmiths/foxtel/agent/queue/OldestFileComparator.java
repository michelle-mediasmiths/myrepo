package com.mediasmiths.foxtel.agent.queue;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Dont persist these for very long, they cache values. Use it for a single sort then throw it away
 */
public class OldestFileComparator implements Comparator<File>
{
	private Map<String, Long> modifiedTimeCache = new HashMap<String, Long>();

	private final static Logger log = Logger.getLogger(OldestFileComparator.class);

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
			log.warn("Error using java.nio to get attributes for path "+path+". Falling back to java.io");

			//error getting attributes using java.nio, fall back to java.io
			final Long lastModified = file.lastModified();
			modifiedTimeCache.put(path, lastModified);
			return lastModified;
		}
	}
}
