package com.mediasmiths.mq.transferqueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import com.mediasmiths.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Keeps track of active transfers, polling until they complete. The transfers are backed by a file queue
 */
@Singleton
public class TransferManager extends Daemon implements StoppableService
{
	private static final Logger log = Logger.getLogger(TransferManager.class);

	private final int sdVideoX;
	private final MayamTaskController taskController;
	private final TasksClient tasksClient;
	private final TransferQueue queue;

	/**
	 * The time to sleep between polling
	 */
	private final Timeout sleepTime = new Timeout(2, TimeUnit.MINUTES);


	@Inject
	public TransferManager(ShutdownManager shutdownManager,
	                       TransferQueue queue,
	                       @Named(MayamClientModule.SETUP_TASKS_CLIENT)TasksClient tasksClient,
	                       MayamTaskController taskController,
	                       @Named("ff.sd.video.imagex") int sdVideoX)
	{
		this.queue = queue;
		this.tasksClient = tasksClient;
		this.taskController = taskController;
		this.sdVideoX = sdVideoX;

		shutdownManager.register(this);

		startThread("TransferManager");
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
						log.error("TransferManager error processing " +
						          item.id +
						          " for asset " +
						          item.assetId +
						          " and peer " +
						          item.assetPeerId, e);
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
	 * 		the transfer item
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
	 * Called once when the transfer is new
	 *
	 * @param item
	 * 		the transfer item
	 */
	protected void added(TransferItem item)
	{
		// TODO any special actions for a new item
		log.info("Transfer " + item.id + " for " + item.assetId + " with peer " + item.assetPeerId + " added");
		try {
			AttributeMap attributes = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.UNMATCHED_MEDIA,
			        item.assetId);
			attributes.setAttribute(Attribute.TASK_STATE, TaskState.SYS_WAIT);
			taskController.updateMapForTask(attributes);
		} catch (MayamClientException e) {
			log.error("Mayam Exception thrown while updating state of unmatched task for asset :" + item.assetId, e);
			e.printStackTrace();
		}
	}


	/**
	 * Called once when the transfer completes successfully
	 *
	 * @param item
	 * 		the transfer item
	 */
	protected void complete(TransferItem item)
	{
		log.info("Transfer item completed: " + item.id);

		try
		{
			// Retrieve task info
			AttributeMap attributes = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.UNMATCHED_MEDIA,
			                                                                          item.assetId);

			final String format = getFormat(attributes);

			// close unmatched task
			closeTask(attributes);

			//get the id of the asset being matched to
			final String peerID = attributes.getAttribute(Attribute.ASSET_PEER_ID).toString();

			//set the format (hd/sd, don't have a way of detecting 3d)
			setFormat(peerID, format);

			// close open ingest task for the target asset
			closeIngestTaskForAsset(peerID, attributes);
		}
		catch (MayamClientException | RemoteException e)
		{
			log.error("Error completing item " +
			          item.id +
			          " for " +
			          item.assetId +
			          " to " +
			          item.assetPeerId +
			          " failed. Failing task.");

			failed(item);

			throw new RuntimeException(e);
		}
	}


	/**
	 * Called once when the transfer fails
	 *
	 * @param item
	 * 		the transfer item
	 */
	protected void failed(TransferItem item)
	{
		log.warn("Transfer item failed: " + item.id);
		try
		{
			AttributeMap attributes = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.UNMATCHED_MEDIA,
			        item.assetId);
			long taskID = attributes.getAttribute(Attribute.TASK_ID);
			taskController.setTaskToErrorWithMessage(taskID, "Error while performing media transfer for asset " + item.assetId);
		} 
		catch (MayamClientException e) {
			log.error("Mayam Exception thrown while setting error state of unmatched task for :" + item.assetId, e);
			e.printStackTrace();
		}
	}


	/**
	 * Queries the transfer for its state. According to Erik at Mayam the logic here is:
	 * <ul>
	 * <li>moveMediaEssence returns without exception if the transfer is complete</li>
	 * <li>moveMediaEssence returns with an exception if the transfer has not completed yet</li>
	 * <li>moveMediaEssence returns with an exception if some error has occurred</li>
	 * </ul>
	 * <p>He plans to add something to distinguish between the exception thrown to indicate transfer is ongoing and the exception
	 * thrown to indicate an error</p>
	 *
	 * @param item
	 * 		the transfer item
	 *
	 * @return the determined transfer state (success, failure, in progress)
	 */
	private TransferState getTransferState(final TransferItem item)
	{
		try
		{
			// Beware: "moveMediaEssence" doesn't do what you think - see above javadoc
			tasksClient.assetApi().moveMediaEssence(MayamAssetType.MATERIAL.getAssetType(),
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


	private void setFormat(String peerID, String format)
	{
		AttributeMap peer;
		try
		{
			peer = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), peerID);

			AttributeMap updateMap = taskController.updateMapForAsset(peer);
			updateMap.setAttribute(Attribute.CONT_FMT, format);
			tasksClient.assetApi().updateAsset(updateMap);
		}
		catch (RemoteException e)
		{
			log.error("Error setting content format on asset " + peerID, e);
		}
	}


	private String getFormat(AttributeMap currentAttributes) throws RemoteException
	{
		String format = "HD";
		try
		{
			FileFormatInfo formatInfo = tasksClient.assetApi().getFormatInfo(MayamAssetType.MATERIAL.getAssetType(),
			                                                                 (String) currentAttributes.getAttribute(Attribute.ASSET_ID));

			if (formatInfo.getImageSizeX() <= sdVideoX)
			{
				format = "SD";
			}
		}
		catch (Exception e)
		{
			log.error("error determining format for asset", e);
			try
			{
				taskController.createWFEErorTask(MayamAssetType.MATERIAL,
				                                 currentAttributes.getAttributeAsString(Attribute.ASSET_SITE_ID),
				                                 "Error determining content format during unmatched asset workflow");
			}
			catch (MayamClientException e1)
			{
				log.error("error creating error task!", e1);
			}
		}

		return format;
	}


	private void copyUnmatchedAttribtes(AttributeMap ingestTaskAttributes, AttributeMap unmatchedAttributes)
	{
		String assetId = ingestTaskAttributes.getAttribute(Attribute.ASSET_ID);
		AssetType assetType = ingestTaskAttributes.getAttribute(Attribute.ASSET_TYPE);
		try
		{
			AttributeMap asset = tasksClient.assetApi().getAsset(assetType, assetId);
			for (Attribute attribute : unmatchedAttributes.getAttributeSet())
			{
				if (asset.getAttribute(attribute) == null)
				{
					asset.setAttribute(attribute, unmatchedAttributes.getAttribute(attribute));
				}
			}
			tasksClient.assetApi().updateAsset(asset);
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while updating asset : " + assetId, e);
		}
	}


	private void closeIngestTaskForAsset(String peerID, AttributeMap unmatchedAttributes)
	{
		AttributeMap task;
		try
		{
			task = taskController.getOnlyTaskForAssetByAssetID(MayamTaskListType.INGEST, peerID);

			copyUnmatchedAttribtes(task, unmatchedAttributes);

			if (task == null)
			{
				log.warn("no ingest task found for assetID " + peerID);
				return;
			}

			TaskState currentState = task.getAttribute(Attribute.TASK_STATE);

			if (TaskState.CLOSED_STATES.contains(currentState))
			{
				log.warn("Ingest task for asset is already in a closed state  " + currentState);
				return;
			}

			log.info(String.format("Import finished for asset %s (%s)", task.getAttributeAsString(Attribute.HOUSE_ID), peerID));
			AttributeMap updateMap = taskController.updateMapForTask(task);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
			updateMap.setAttribute(Attribute.INGEST_NOTES, "Unmatched asset matched to this placeholder");
			taskController.saveTask(updateMap);
		}
		catch (MayamClientException e)
		{
			log.error("Error closing ingest task for asset " + peerID, e);
		}
	}


	protected void closeTask(AttributeMap messageAttributes)
	{
		try
		{
			messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			taskController.saveTask(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception removing task " + messageAttributes.getAttributeAsString(Attribute.TASK_ID), e);
		}
	}


	@Override
	public void shutdown()
	{
		this.stopThread();
	}
}
