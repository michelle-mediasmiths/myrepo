package com.mediasmiths.foxtel.agent;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class WatchFolders extends HashMap<String, WatchFolder>
{

	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(WatchFolders.class);

	public WatchFolders()
	{
		super();
	}

	public WatchFolders(List<String> sourcePaths)
	{
		super();

		for (String s : sourcePaths)
		{
			this.add(new WatchFolder(s));
		}
	}
	
	public boolean isAo(String src)
	{

		for (WatchFolder f : this.values())
		{
			if (src.startsWith(f.getSource()))
			{
				log.trace(String.format("%s prefix of %s", f.getSource(), src));
				return f.isAO();
			}
			else
			{
				log.trace(String.format("%s not prefix of %s", f.getSource(), src));
			}
		}

		log.warn(String.format("non recognised source directory %s passed as argument to isAo()", src));
		return false;

	}

	public String getNameForWatchFolder(String src){
		for (WatchFolder f : this.values())
		{
			if (src.startsWith(f.getSource()))
			{
				log.trace(String.format("%s prefix of %s", f.getSource(), src));
				return f.getName();
			}
			else
			{
				log.trace(String.format("%s not prefix of %s", f.getSource(), src));
			}
		}

		log.warn(String.format("non recognised source directory %s passed as argument to getNameForWatchFolder()", src));
		return FilenameUtils.getBaseName(src);
	}

	public boolean isRuzz(String src)
	{

		for (WatchFolder f : this.values())
		{
			if (src.startsWith(f.getSource()))
			{
				log.trace(String.format("%s prefix of %s", f.getSource(), src));
				return f.isRuzz();
			}
			else
			{
				log.trace(String.format("%s not prefix of %s", f.getSource(), src));
			}
		}

		log.warn(String.format("non recognised source directory %s passed as argument to isRuzz()", src));
		return false;

	}

	public String destinationFor(String src)
	{

		for (WatchFolder f : this.values())
		{

			if (src.startsWith(f.getSource()))
			{
				log.trace(String.format("%s prefix of %s", f.getSource(), src));
				return f.getDelivery();
			}
			else
			{
				log.trace(String.format("%s not prefix of %s", f.getSource(), src));
			}
		}

		log.error(String.format("No delivery location configured for %s", src));
		throw new IllegalArgumentException(String.format("No delivery location configured for %s", src));

	}

	public void add(WatchFolder watchFolder)
	{
		log.debug(String.format(
				"Adding watchfolder source: %s  delivery %s isAo %b isRuzz %b",
				watchFolder.getSource(),
				watchFolder.getDelivery(),
				watchFolder.isAO(),
				watchFolder.isRuzz()));
		super.put(watchFolder.getSource(),watchFolder);
	}

}
