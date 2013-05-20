package com.mediasmiths.foxtel.tx.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class ActiveFXPTransfer implements Runnable
{

	private FTPClient proxy;
	private FTPClient target;
	private Long taskID;
	private TxFtpDelivery ftpDelivery;

	private final static Logger log = Logger.getLogger(ActiveFXPTransfer.class);

	public ActiveFXPTransfer(FTPClient proxy, FTPClient target, Long taskID,TxFtpDelivery ftpDelivery)
	{
		this.proxy = proxy;
		this.target = target;
		this.taskID = taskID;
		this.ftpDelivery=ftpDelivery;
	}

	@Override
	public void run()
	{
		try
		{
			log.info("Starting fxp transfer for task " + taskID);
			ftpDelivery.setTransferStatus(taskID, TransferStatus.STARTED);
			
			proxy.completePendingCommand();
			target.completePendingCommand();

			log.info(String.format("Fxp transfer for task %d complete", taskID));
			ftpDelivery.setTransferStatus(taskID, TransferStatus.FINISHED);
		}
		catch (IOException e)
		{
			log.error("Error during fxp transfer for task " + taskID, e);
			try
			{
				ftpDelivery.setTransferStatus(taskID, TransferStatus.FAILED);
			}
			catch (IOException e1)
			{
				log.error("IOException setting transfer status",e);
				//... what can we do?
			}
		}
		finally
		{
			TxFtpDelivery.disconnect(proxy);
			TxFtpDelivery.disconnect(target);
		}
	}
	
	public void tryAbort()
	{
		try
		{
			proxy.abor(); //abor() will wait for a reply, could this still block?
			target.abor();
		}
		catch (IOException e)
		{
			log.warn("exception during abor()", e);
		}
		finally
		{
			TxFtpDelivery.disconnect(proxy);
			TxFtpDelivery.disconnect(target);
		}
	}
}
