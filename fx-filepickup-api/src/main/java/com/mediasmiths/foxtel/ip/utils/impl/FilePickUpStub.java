package com.mediasmiths.foxtel.ip.utils.impl;

import com.mediasmiths.foxtel.ip.utils.FilePickUp;
import org.apache.log4j.Logger;

import javax.ws.rs.QueryParam;

/**
 * Stub Implementation of the file pick up API.
 *
 * Author: Harmer
 */
public class FilePickUpStub implements FilePickUp
{

	private static final Logger logger = Logger.getLogger(FilePickUpStub.class);

	@Override
	public void newFile(@QueryParam("name") final String filePath)
	{
       logger.info("New File: " + filePath);
	}

	@Override
	public String getPending()
	{
		return null;
	}

	@Override
	public String getStatus()
	{
		return null;
	}

	@Override
	public void clear()
	{
	}
}
