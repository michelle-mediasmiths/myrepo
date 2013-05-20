package com.mediasmiths.foxtel.tx.ftp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;

public class TxFtpDelivery implements StoppableService
{
	private final static Logger log = Logger.getLogger(TxFtpDelivery.class);
	
	ActiveFXPTransfers activeTransfers;
	private final String fxpStatusFolder;
	
	@Inject
	public TxFtpDelivery(ActiveFXPTransfers activeTransfers, ShutdownManager shutDownManager,@Named("fxp.transfer.status.folder") String fxpStatusFolder, @Named("fxp.transfer.fail.existing.on.startup") boolean failexistingTransfersOnStartup){
		this.activeTransfers=activeTransfers;
		shutDownManager.register(this);
		this.fxpStatusFolder=fxpStatusFolder;
		if (failexistingTransfersOnStartup)
		{
			findAndFailActiveTransfers();
		}
	}
	
	private void findAndFailActiveTransfers()
	{
		Collection<File> files = FileUtils.listFiles(new File(fxpStatusFolder), new IOFileFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith("txt");
			}
			
			@Override
			public boolean accept(File file)
			{
				return file.getName().toLowerCase().endsWith("txt");
			}
		}, new IOFileFilter()
		{
			
			@Override
			public boolean accept(File dir, String name)
			{
				return false;
			}
			
			@Override
			public boolean accept(File file)
			{
				return false;
			}
		});		
		
		for (File file : files)
		{
			try
			{
				String contents = FileUtils.readFileToString(file);

				if (contents.equals(TransferStatus.STARTED.name())) //started transfer found, set to failed as there is no way to resume
				{
					try
					{
						FileUtils.writeStringToFile(file, TransferStatus.FAILED.name());
					}
					catch (IOException e)
					{
						log.error("error writing to file " + file.getAbsolutePath(), e);
					}
				}

			}
			catch (IOException e)
			{
				log.error("error reading: " + file.getAbsolutePath(), e);
			}
		}
	}
	
	public boolean fileExists(String remotePath, String remoteFileName, String host, String user, String pass) throws IOException
	{
		FTPClient client = new FTPClient();
		connect(host, client);

		if (!client.login(user, pass))
		{
			log.error("Could not login to " + host);
			throw new IOException("Could not login to " + host);
		}

		log.debug("Changing to destination directory on target");

		boolean changeWorkingDirectory = client.changeWorkingDirectory(remotePath);

		if (changeWorkingDirectory)
		{
			log.debug("Successfully changed working directory on target");
		}
		else
		{
			log.error("failed to change to destination directory on target");
			throw new IOException("failed to change to destination directory on target");
		}
		try
		{
			FTPFile[] listFiles = client.listFiles();

			for (FTPFile ftpFile : listFiles)
			{
				if (ftpFile.getName().equals(remoteFileName))
				{
					return true;
				}

			}
		}
		finally
		{
			disconnect(client);
		}
		return false;
	}

	public boolean startFtpProxyTransfer(
			String sourcePath,
			String sourceFileName,
			String proxyHost,
			String proxyUser,
			String proxyPass,
			String targetPath,
			String targetFile,
			String targetHost,
			String targetUser,
			String targetPass, Long taskID)
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

		boolean proxyRetreive =false;
		boolean targetStore =false;
		
		boolean proxyConnected = connect(proxyHost, proxy);
		
		if(proxyConnected==false)
		{
			return false;
		}
		
		boolean targetConnected = connect(targetHost, target);
		
		if (targetConnected == false)
		{
			disconnect(proxy);
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
				log.error("failed to change to source directory on proxy");
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
				log.error("failed to change to destination directory on target");
				return false;
			}

			log.debug("Setting type binary on proxy");
			proxy.setFileType(FTP.BINARY_FILE_TYPE);

			log.debug("Setting type binary on target");
			target.setFileType(FTP.BINARY_FILE_TYPE);

			log.debug("Entering Remote Passive Mode on target");
			target.enterRemotePassiveMode();
			log.debug("Entering remote active mode on proxy");
			proxy.enterRemoteActiveMode(InetAddress.getByName(target.getPassiveHost()), target.getPassivePort());

			log.debug("remoteRetreive: " + sourceFileName);
			log.debug("remoteStore: " + targetFile);

			proxyRetreive = proxy.remoteRetrieve(sourceFileName);
			targetStore = target.remoteStore(targetFile);
		}
		catch (Exception e)
		{
			log.error("Exception preparing transfer", e);
			disconnect(proxy);
			disconnect(target);
		}
		
		log.debug(String.format("proxy retreive %b", proxyRetreive));
		log.debug(String.format("target store %b", targetStore));

		
		if (proxyRetreive && targetStore)
		{
			log.info("Transfer ready to start");
			
			ActiveFXPTransfer trans = new ActiveFXPTransfer(proxy, target, taskID, this);
			activeTransfers.put(taskID,trans);
			return true;
		}
		else
		{
			log.error("Couldn't initiate transfer.  Check that filenames are valid.");
			return false;
		}
		
	}

	private boolean connect(String host, FTPClient client)
	{
		// connect to server
		try
		{
			int reply;
			client.connect(host);
			reply = client.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply))
			{
				client.disconnect();
				log.error("FTP server " + host + "refused connection.");
				return false;
			}
			else{
				return true;
			}
		}
		catch (IOException e)
		{
			if (client.isConnected())
			{
				try
				{
					client.disconnect();
				}
				catch (IOException f)
				{
					// do nothing
				}
			}
			log.error("Could not connect to " + host, e);
			return false;
		}
	}

	protected static void disconnect(FTPClient client)
	{
		try
		{
			if (client.isConnected())
			{
				client.logout();
				client.disconnect();
			}
		}
		catch (IOException e1)
		{
			// do nothing
		}
	}

	
	
	protected synchronized void setTransferStatus(Long taskID, TransferStatus status) throws IOException{

		File statusFile = getStatusFileForTask(taskID);
		FileUtils.writeStringToFile(statusFile, status.name());
	}

	private File getStatusFileForTask(Long taskID)
	{
		File ticketFile = new File(fxpStatusFolder, String.format("%s.txt",taskID));
		return ticketFile;
	}
	
	public synchronized TransferStatus getTransferStatus(Long taskID) throws IOException{
	
		File statusFile = getStatusFileForTask(taskID);
		String contents = FileUtils.readFileToString(statusFile);
		try
		{
			return TransferStatus.valueOf(contents);
		}
		catch (Exception e)
		{
			log.error("Exception getting TaskStatus from "+contents+" pessimistically assuming status of FAILED",e);
			return TransferStatus.FAILED;
		}
	}
	
	public void removeTransfer(Long taskID)
	{
		File statusFile = getStatusFileForTask(taskID);
		FileUtils.deleteQuietly(statusFile);
	}

	@Override
	public void shutdown()
	{
		for (ActiveFXPTransfer transfer : activeTransfers.values())
		{
			transfer.tryAbort();
		}
	}

	public void abortTransfer(Long taskID) throws IOException
	{
		ActiveFXPTransfer activeFXPTransfer = activeTransfers.get(taskID);
		activeFXPTransfer.tryAbort();
		setTransferStatus(taskID, TransferStatus.ABORTED);
	}
}
