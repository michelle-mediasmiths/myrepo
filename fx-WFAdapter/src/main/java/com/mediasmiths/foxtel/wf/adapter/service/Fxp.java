package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class Fxp
{
	public static void main(String[] args)
	{
		System.out.println(ftpProxyTransfer(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]));
	}

	private final static Logger log = Logger.getLogger(Fxp.class);

	public static boolean ftpProxyTransfer(
			String sourcePath,
			String sourceFileName,
			String proxyHost,
			String proxyUser,
			String proxyPass,
			String targetPath,
			String targetFile,
			String targetHost,
			String targetUser,
			String targetPass)
	{

		log.debug(String.format(
				"sourcePath : {%s}, sourceFileName : {%s},\nproxyHost : {%s},\nproxyUser : {%s},\nproxyPass : {%s},\ntargetPath : {%s},\ntargetFile : {%s}\ntargetHost : {%s},\ntargetUser : {%s},\ntargetPass : {%s},\n",
				sourcePath,
				sourceFileName,
				proxyHost,
				proxyUser,
				proxyPass,
				targetPath,
				targetFile,
				targetHost,
				targetUser,
				targetPass));

		ProtocolCommandListener listener = new PrintCommandListener(new PrintWriter(System.out), true);
		FTPClient proxy = new FTPClient();
		proxy.addProtocolCommandListener(listener);
		FTPClient target = new FTPClient();
		target.addProtocolCommandListener(listener);

		// connect to proxy server
		try
		{
			int reply;
			proxy.connect(proxyHost);
			reply = proxy.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply))
			{
				proxy.disconnect();
				log.error("FTP server " + proxyHost + "refused connection.");
				return false;
			}
		}
		catch (IOException e)
		{
			if (proxy.isConnected())
			{
				try
				{
					proxy.disconnect();
				}
				catch (IOException f)
				{
					// do nothing
				}
			}
			log.error("Could not connect to " + proxyHost, e);
			return false;
		}

		// connect to target server
		try
		{
			int reply;
			target.connect(targetHost);
			reply = target.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply))
			{
				target.disconnect();
				log.error("FTP server " + targetHost + "refused connection.");
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
			log.error("Could not connect to " + targetHost, e);
			return false;
		}

		try
		{
			if (!proxy.login(proxyUser, proxyPass))
			{
				log.error("Could not login to " + proxyHost);
				return false;
			}

			if (!target.login(targetUser, targetPass))
			{
				log.error("Could not login to " + targetHost);
				return false;
			}

			log.debug("Changing to source directory on proxy");
			boolean changeWorkingDirectory = proxy.changeWorkingDirectory(sourcePath);

			if (changeWorkingDirectory)
			{
				log.debug("Successfully changed working directory on proxy");
			}
			else
			{
				log.error("failed to chagne to source directory on proxy");
				return false;
			}
			
			log.debug("Changing to destination directory on target");
			
			changeWorkingDirectory = target.changeWorkingDirectory(targetPath);

			if (changeWorkingDirectory)
			{
				log.debug("Successfully changed working directory on target");
			}
			else
			{
				log.error("failed to chagne to source directory on target");
				return false;
			}
			
			log.debug("Entering Remote Passive Mode on target");
			target.enterRemotePassiveMode();
			log.debug("Entering remote active mode on proxy");
			proxy.enterRemoteActiveMode(InetAddress.getByName(target.getPassiveHost()), target.getPassivePort());

			log.debug("remoteRetreive: " + sourceFileName);
			log.debug("remoteStore: " + targetFile);

			boolean proxyRetreive = proxy.remoteRetrieve(sourceFileName);
			boolean proxyStore = proxy.remoteStore(targetFile);

			log.debug(String.format("proxy retreive %b", proxyRetreive));
			log.debug(String.format("proxy store %b", proxyStore));

			if (proxyRetreive && proxyStore)
			{
				proxy.completePendingCommand();
				target.completePendingCommand();
			}
			else
			{
				log.error("Couldn't initiate transfer.  Check that filenames are valid.");
				return false;
			}

		}
		catch (Exception e)
		{
			log.error("exception performing transfer", e);
		}
		finally
		{
			try
			{
				if (proxy.isConnected())
				{
					proxy.logout();
					proxy.disconnect();
				}
			}
			catch (IOException e)
			{
				// do nothing
			}

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
