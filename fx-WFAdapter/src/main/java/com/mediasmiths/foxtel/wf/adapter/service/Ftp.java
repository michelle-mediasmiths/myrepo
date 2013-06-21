package com.mediasmiths.foxtel.wf.adapter.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;


public class Ftp
{

	private final static Logger log = Logger.getLogger(Ftp.class);

	public static void main(String[] args)
	{
		System.out.println(ftpTransfer(args[0], args[1], args[2], args[3], args[4], args[5]));
	}
	
	public static boolean ftpTransfer(
			String host,
			String user,
			String password,
			String localPath,
			String remoteFolder,
			String remoteFileName)
	{

		log.debug(String.format(
				"host : {%s}, user : {%s}, password : {%s}, localPath : {%s}, remoteFolder : {%s}, remoteFileName : {%s}",
				host,
				user,
				password,
				localPath,
				remoteFolder,
				remoteFileName));

		ProtocolCommandListener listener = new PrintCommandListener(new PrintWriter(System.out), true);
		FTPClient target = new FTPClient();
		target.addProtocolCommandListener(listener);

		// connect to target server
		try
		{
			int reply;
			target.connect(host);
			reply = target.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply))
			{
				target.disconnect();
				log.error("FTP server " + host + "refused connection.");
				return false;
			}
		}
		catch (IOException e)
		{
			if (target.isConnected())
			{
				try
				{
					target.disconnect();
				}
				catch (IOException f)
				{
					// do nothing
				}
			}
			log.error("Could not connect to " + host, e);
			return false;
		}
		try
		{
			if (!target.login(user, password))
			{
				log.error("Could not login to " + host);
				return false;
			}

			log.debug("Changing to destination directory on target");

			if (!StringUtils.isEmpty(remoteFolder))
			{

				boolean changeWorkingDirectory = target.changeWorkingDirectory(remoteFolder);

				if (changeWorkingDirectory)
				{
					log.debug("Successfully changed working directory on target");
				}
				else
				{
					log.error("failed to change to destination directory on target");
					return false;
				}
			}
			
			log.debug("Setting type binary on target");
			target.setFileType(FTP.BINARY_FILE_TYPE);

			// transfer
			InputStream input = new FileInputStream(localPath);
			target.storeFile(remoteFileName,input);
            input.close();

		}
		catch (Exception e)
		{
			log.error("exception performing transfer", e);
		}
		finally
		{
			try
			{
				if (target.isConnected())
				{
					target.logout();
					target.disconnect();
				}
			}
			catch (IOException e)
			{
				// do nothing
			}
		}
		return true;
	}
}
