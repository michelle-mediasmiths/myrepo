package com.mediasmiths.mq.handlers.unmatched;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class UnmatchedTaskUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedTaskUpdateHandler.class);

	@Inject
	@Named("ff.sd.video.imagex")
	private int sdVideoX = 720;

	@Override
	public String getName()
	{
		return "Unmatch Task Update";
	}

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		try
		{
			if (assetType.equals(MayamAssetType.MATERIAL.getAssetType())
					&& attributeChanged(Attribute.ASSET_PEER_ID, before, after, currentAttributes))
			{

				log.info(String.format(
						"Unmatched task for material %s updated with match to asset %s",
						currentAttributes.getAttributeAsString(Attribute.HOUSE_ID),
						currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID)));

				String format = getFormat(currentAttributes);
				removeUnmatchedAssetFromTaskLists(assetID);

				// Move media
				tasksClient.assetApi().moveMediaEssence(
						MayamAssetType.MATERIAL.getAssetType(),
						currentAttributes.getAttribute(Attribute.ASSET_ID).toString(),
						MayamAssetType.MATERIAL.getAssetType(),
						currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString());

				// close unmatched task
				closeTask(currentAttributes);

				//get the id of the asset being matched to
				String peerID = currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString();

				//set the format (hd/sd, don't have a way of detecting 3d)
				setFormat(peerID, format);

				// close open ingest task for the target asset
				closeIngestTaskForAsset(peerID, currentAttributes);
			}
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling unmatched task update Message : " + e.getMessage(), e);
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
			FileFormatInfo formatInfo = tasksClient.assetApi().getFormatInfo(
					MayamAssetType.MATERIAL.getAssetType(),
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
				taskController.createWFEErorTask(
						MayamAssetType.MATERIAL,
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

	private void removeUnmatchedAssetFromTaskLists(String assetID)
	{
		try
		{
			// Remove unmatched task from any task lists
			AttributeMap filterEqualities = tasksClient.createAttributeMap();
			filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
			FilterCriteria criteria = new FilterCriteria();
			criteria.setFilterEqualities(filterEqualities);
			FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 50, 0);

			if (existingTasks.getTotalMatches() > 0)
			{
				List<AttributeMap> tasks = existingTasks.getMatches();
				for (int i = 0; i < existingTasks.getTotalMatches(); i++)
				{
					AttributeMap task = tasks.get(i);
					TaskState currentState = task.getAttribute(Attribute.TASK_STATE);

					if (!TaskState.CLOSED_STATES.contains(currentState))
					{

						task.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
						try
						{
							taskController.saveTask(task);
						}
						catch (Exception e)
						{
							log.error("error removing task", e);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("error performing task search", e);
		}
	}

	private void copyUnmatchedAttribtes(AttributeMap ingestTaskAttributes, AttributeMap unmatchedAttributes)
	{
		String assetId = ingestTaskAttributes.getAttribute(Attribute.ASSET_ID);
		AssetType assetType = ingestTaskAttributes.getAttribute(Attribute.ASSET_TYPE);
		AttributeMap asset = tasksClient.assetApi().getAsset(assetType, assetId);
		for (Attribute attribute:unmatchedAttributes.getAttributeSet())
		{
			if (asset.getAttribute(attribute) == null)
			{
				asset.setAttribute(attribute, unmatchedAttributes.getAttribute(attribute));
			}
		}
		tasksClient.assetApi().updateAsset(asset);
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
			log.error("Error closing ingest task for asset " + peerID,e);
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
			log.error("Exception removing task " + getTaskType(), e);
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}
}
