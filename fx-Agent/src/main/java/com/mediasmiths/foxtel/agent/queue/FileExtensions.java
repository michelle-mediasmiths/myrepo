package com.mediasmiths.foxtel.agent.queue;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public enum FileExtensions
{

	XML("XML"), MXF("MXF");

    private final String text;

	FileExtensions(final String txt)
	{
		this.text = txt;
	}


	public static FileExtensions getExtension(File path)
	{
        if (path == null)
	        throw new IllegalArgumentException("Cannot get the file extension for a null file path");

		if (path.getName() == null)
			throw new IllegalArgumentException("Cannot get the file extension for an empty file path");

		return fromString(FilenameUtils.getExtension(path.getName()));

	}

	public static FileExtensions getExtension(String path)
	{
		if (path == null || path.length() == 0)
			throw new IllegalArgumentException("Cannot get the file extension for a null/empty file path");

		return fromString(FilenameUtils.getExtension(path));
	}

	public static FileExtensions fromString(String extension)
	{
		return valueOf(extension.toUpperCase());
	}

}
