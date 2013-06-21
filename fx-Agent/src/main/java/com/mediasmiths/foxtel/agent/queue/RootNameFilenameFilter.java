package com.mediasmiths.foxtel.agent.queue;

import com.mediasmiths.std.io.filter.FilenameExtensionFilter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * define a filename filter for suffixes and root file names
 * suffixes are converted to lower case so .XML and .xml are identical.
 */
public class RootNameFilenameFilter extends FilenameExtensionFilter implements FilenameFilter
{
	private String rootName;

	/**
	 *
	 * @param rootName filenames only with this root name
	 * @param extensions filenames only with this extension
	 */
	public RootNameFilenameFilter(final String rootName, final String... extensions)
	{
		super(false, extensions);
		this.rootName = rootName;
	}

	@Override
	public boolean accept(final File dir, final String name)
	{
		String fileBaseName = FilenameUtils.getBaseName(name);
		return rootName.equals(fileBaseName) && super.accept(dir, name);
	}
}
