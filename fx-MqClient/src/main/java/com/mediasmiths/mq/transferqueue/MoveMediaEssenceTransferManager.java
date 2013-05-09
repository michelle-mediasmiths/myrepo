package com.mediasmiths.mq.transferqueue;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import com.mediasmiths.std.threading.Timeout;

public abstract class MoveMediaEssenceTransferManager  extends Daemon implements StoppableService
{

	private static final Logger log = Logger.getLogger(MoveMediaEssenceTransferManager.class);
	
	protected final TasksClientVeneer tasksClient;
	private final TransferQueue queue;
	
	/**
	 * The time to sleep between polling
	 */
	private final Timeout sleepTime = new Timeout(2, TimeUnit.MINUTES);
	
	public MoveMediaEssenceTransferManager(
			TransferQueue queue,
			TasksClientVeneer tasksClient)
	{
		this.queue = queue;
		this.tasksClient = tasksClient;
	}

	
	public void add(TransferItem item)
	{
		// Add the item to the queue
		queue.add(item);

		// Perform any new item operations
		added(item);
	}
	
	@Override
	public void run()
	{
		while (isRunning())
		{
			try
			{
				// Process each item individually, logging errors (and continuing) in the event of an Exception
				for (TransferItem item : queue.getItems())
				{
					if (!isRunning())
						break;

					try
					{
						process(item);
					}
					catch (Throwable e)
					{
						log.error("TransferManager error processing " + item.id + " for asset " + item.assetId + " and peer "
								+ item.assetPeerId, e);
					}
				}
			}
			catch (Throwable e)
			{
				log.error("TransferManager top-level error", e);
			}

			// Wait and go around the loop again
			if (isRunning())
				sleepTime.sleep();
		}
	}

	/**
	 * Called periodically for each item
	 * 
	 * @param item
	 *            the transfer item
	 */
	public void process(TransferItem item)
	{
		if (log.isDebugEnabled())
			log.debug("Checking transfer " + item.id);

		// Check if the transfer's completed/failed
		final TransferState state = getTransferState(item);

		if (log.isDebugEnabled())
			log.debug("Poll " + item.id + " - state was " + state);

		// If the item has reached a terminal state then we should take a finalisation action
		if (state == TransferState.COMPLETE)
		{
			queue.remove(item);
			complete(item);
		}
		else if (state == TransferState.FAILED)
		{
			queue.remove(item);
			failed(item);
		}
	}
	
	/**
	 * Queries the transfer for its state. According to Erik at Mayam the logic here is:
	 * <ul>
	 * <li>moveMediaEssence returns without exception if the transfer is complete</li>
	 * <li>moveMediaEssence returns with an exception if the transfer has not completed yet</li>
	 * <li>moveMediaEssence returns with an exception if some error has occurred</li>
	 * </ul>
	 * <p>
	 * He plans to add something to distinguish between the exception thrown to indicate transfer is ongoing and the exception thrown to indicate an error
	 * </p>
	 * 
	 * @param item
	 *            the transfer item
	 * 
	 * @return the determined transfer state (success, failure, in progress)
	 */
	private TransferState getTransferState(final TransferItem item)
	{
	
		try
		{
			// Beware: "moveMediaEssence" doesn't do what you think - see above javadoc
			tasksClient.assetApi().moveMediaEssence(
					MayamAssetType.MATERIAL.getAssetType(),
					item.assetId,
					MayamAssetType.MATERIAL.getAssetType(),
					item.assetPeerId);
			return TransferState.COMPLETE;
		}
		catch (RemoteException e)
		{
			log.debug("moveMediaEssence returned exception", e);

			// If the transfer has timed out we should fail
			if (item.timeout.getTime() < System.currentTimeMillis())
			{
				return TransferState.FAILED;
			}
			// TODO once Mayam distinguish between errors+incomplete transfer cases we should check the Mayam exception to determine if it's a genuine failure
			else
			{
				return TransferState.IN_PROGRESS;
			}
		}
	}
	
	protected abstract void added(final TransferItem item);
	protected abstract void complete(final TransferItem item);
	protected abstract void failed(final TransferItem item);
}
