package com.foxtel.ip.mail.process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.foxtel.ip.mail.data.FileAndFolderLocations;
import com.google.inject.Inject;

public class ReadBodyFile
{
	private static final transient Logger logger = Logger.getLogger(ReadBodyFile.class);

	@Inject(optional = false)
	FileAndFolderLocations folderLocation;

	/**
	 * Reads body from file
	 * 
	 * @param fileLocation
	 * @return
	 */
	public String readFile(String fileLocation)
	{
		BufferedReader br = null;
		try
		{
			if (logger.isTraceEnabled())
				logger.info("Read Body file called, File name: " + folderLocation.bodyFolderLocation + fileLocation + ".html");

			br = new BufferedReader(new FileReader(folderLocation.bodyFolderLocation + fileLocation + ".html"));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null)
			{
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}

			String everything = sb.toString();
			if (logger.isTraceEnabled())
				logger.info("Body found: " + everything);
			return everything;
		}
		catch (FileNotFoundException e)
		{
			logger.error("Could not find: " + folderLocation.bodyFolderLocation + fileLocation + ".html. Error thrown ", e);
		}
		catch (IOException e)
		{
			logger.error(e);
		}
		finally
		{
			try
			{
				br.close();
			}
			catch (IOException e)
			{
				logger.error(e);
			}
		}
		return null;

	}
}
