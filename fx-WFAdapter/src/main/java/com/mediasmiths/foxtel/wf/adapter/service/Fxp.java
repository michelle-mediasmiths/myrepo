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
	public static void main(String [] args){
		System.out.println(ftpProxyTransfer(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]));
	}
	
	private final static Logger log = Logger.getLogger(Fxp.class);
	
	public static boolean ftpProxyTransfer(String sourceFileName,String proxyHost,String proxyUser, String proxyPass, String targetPath, String targetHost, String targetUser, String targetPass)
	{
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
			if (!proxy.login(proxyUser,proxyPass))
			{
				log.error("Could not login to " + proxyHost);
				return false;
			}

			if (!target.login(targetUser, targetPass))
			{
				log.error("Could not login to " + targetHost);
				return false;
			}

			target.enterRemotePassiveMode();
			proxy.enterRemoteActiveMode(InetAddress.getByName(target.getPassiveHost()), target.getPassivePort());

			if (proxy.remoteRetrieve(sourceFileName)
					&& proxy.remoteStore(targetPath))
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
