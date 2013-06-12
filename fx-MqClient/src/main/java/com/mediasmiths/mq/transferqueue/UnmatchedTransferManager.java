package com.mediasmiths.mq.transferqueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Keeps track of active transfers, polling until they complete. The transfers are backed by a file queue
 */
@Singleton
public class UnmatchedTransferManager extends MoveMediaEssenceTransferManager
{
	public static final Logger log = Logger.getLogger(UnmatchedTransferManager.class);

	public final MayamTaskController taskController;

	@Inject
	public UnmatchedTransferManager(
			ShutdownManager shutdownManager,
			TransferQueue queue,
			TasksClientVeneer tasksClient,
			MayamTaskController taskController)
	{
		super(queue, tasksClient);
		this.taskController = taskController;
		shutdownManager.register(this);
		startThread("UnmatchedTransferManager");
	}

	/**
	 * Called once when the transfer is new
	 * 
	 * @param item
	 *            the transfer item
	 */
	protected void added(TransferItem item)
	{
		log.info("Transfer " + item.id + " for " + item.assetId + " with peer " + item.assetPeerId + " added");
	}

	/**
	 * Called once when the transfer completes successfully
	 * 
	 * @param item
	 *            the transfer item
	 */
	protected void complete(final TransferItem item)
	{
		log.info("Transfer item completed: " + item.id);

		final String unmatchedAssetID = item.assetId;
		final String unmatchedAssetHouseID = item.assetHouseId;
		final String assetPeerID = item.assetPeerId;

		try
		{
			// move media essence has finished for matching of unmatched asset to placeholder

			// get all the unmatched tasks for the affected asset (there will usually only be one but there could potentially be more)
			List<AttributeMap> allUnmatchedTasks;
			
			if(unmatchedAssetHouseID == null){
				log.warn("house id null for transfer item, will not be abe to complete unmatched tasks");
				allUnmatchedTasks = Collections.emptyList();
			}
			else{
				allUnmatchedTasks = taskController.getUmatchedTasksForItem(unmatchedAssetHouseID);
			}

			// close open ingest task for the target asset
			log.debug(String.format("Closing ingest task for asset with assetId %s", assetPeerID));
			taskController.closeIngestTaskForAssetAsUmatched(assetPeerID);

			// close purge candidate task for unmatched asset
			log.debug(String.format("Closing purge candidate task for asset with assetId %s", unmatchedAssetID));
			closePurgeCandidateTaskForAsset(unmatchedAssetID);
				
			// finish any unmatched task for asset
			for (AttributeMap unmatchedTask : allUnmatchedTasks)
			{
				taskController.changeStatus(unmatchedTask, TaskState.FINISHED);
			}
		}
		catch (MayamClientException e)
		{
			log.error(String.format(
					"Error completing item %s for %s to %s failed. Failing task.",
					item.id,
					unmatchedAssetID,
					item.assetPeerId));
			failed(item);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Called once when the transfer fails
	 * 
	 * @param item
	 *            the transfer item
	 */
	protected void failed(TransferItem item)
	{
		log.warn("Transfer item failed: " + item.id);
		try
		{
			final String unmatchedAssetHouseID = item.assetHouseId;

			// find and fail any unmatched tasks for unmatched asset
			List<AttributeMap> allUnmatchedTasks;
			
			if(unmatchedAssetHouseID == null){
				log.warn("house id null for transfer item, will not be abe to complete unmatched tasks");
				allUnmatchedTasks = Collections.emptyList();
			}
			else{
				allUnmatchedTasks = taskController.getUmatchedTasksForItem(unmatchedAssetHouseID);
			}

			for (AttributeMap task : allUnmatchedTasks)
			{
				taskController.setTaskToWarningWithMessage(task, "Error while performing media transfer for asset " + item.assetId);
			}
		}
		catch (MayamClientException e)
		{
			log.error("Mayam Exception thrown while setting error state of unmatched task for :" + item.assetId, e);
		}
	}

	public void closePurgeCandidateTaskForAsset(String assetID)
	{
		List<AttributeMap> tasks;
		try
		{
			log.info("closing purge candidate tasks for asset "+assetID);
			tasks=taskController.getOpenTasksForAsset(MayamTaskListType.PURGE_CANDIDATE_LIST,Attribute.ASSET_ID,assetID);

			if (tasks == null || tasks.size()==00)
			{
				log.warn("no purge candidate task found for assetID " + assetID);
				return;
			}

			for (AttributeMap task : tasks)
			{

				TaskState currentState = task.getAttribute(Attribute.TASK_STATE);

				if (MayamTaskController.END_STATES.contains(currentState))
				{
					log.warn("Purge Candidate task for asset is already in a closed state  " + currentState);
					return;
				}
				taskController.cancelTask(task);
			}
		}
		catch (MayamClientException e)
		{
			log.error("Error closing purge candidate task for asset " + assetID, e);
			try
			{
				taskController.createWFEErrorTaskByAssetID(MayamAssetType.MATERIAL,assetID, "Error closing purge candidate task for asset ");
			}
			catch (MayamClientException e1)
			{
				log.error("Error creating error task for asset!",e1);
				try
				{
					taskController.createWFEErrorTaskNoAsset(assetID, assetID, "Error closing purge candidate task for asset");
				}
				catch (MayamClientException e2)
				{
					log.error("Error creating error task with no asset");
					//give up
				}
			}
		}
	}
	
	public void closeTask(AttributeMap messageAttributes)
	{
		log.debug("Closing task");
		try
		{

			messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			taskController.saveTask(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception removing task " + messageAttributes.getAttributeAsString(Attribute.TASK_ID), e);
		}
		log.debug("Task closed without throwing exception");
	}

	@Override
	public void shutdown()
	{
		this.stopThread();
	}
}
