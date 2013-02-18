package com.mediasmiths.mq.handlers.unmatched;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class UnmatchedTaskUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedTaskUpdateHandler.class);

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

				// Move media
				tasksClient.assetApi().moveMediaEssence(
						MayamAssetType.MATERIAL.getAssetType(),
						currentAttributes.getAttribute(Attribute.ASSET_ID).toString(),
						MayamAssetType.MATERIAL.getAssetType(),
						currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString());

				// close unmatched task
				closeTask(currentAttributes);

				String peerID = currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString();

				// close open ingest task for the target asset
				closeIngestTaskForAsset(peerID);

				// Create QC task
				AttributeMap matchedAsset = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), peerID);
				taskController.createQCTaskForMaterial(
						matchedAsset.getAttributeAsString(Attribute.HOUSE_ID),
						(Date) matchedAsset.getAttribute(Attribute.COMPLETE_BY_DATE),
						matchedAsset.getAttributeAsString(Attribute.QC_PREVIEW_RESULT),
						matchedAsset);
			}
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling unmatched task update Message : " + e.getMessage(), e);
		}

	}

	private void closeIngestTaskForAsset(String peerID)
	{
		AttributeMap task;
		try
		{
			task = taskController.getTaskForAssetByAssetID(MayamTaskListType.INGEST, peerID);

			if(task == null){
				log.warn("no ingest task found for assetID "+ peerID);
				return;
			}
			
			TaskState currentState = task.getAttribute(Attribute.TASK_STATE);
			
			if(TaskState.CLOSED_STATES.contains(currentState)){
				log.warn("Ingest task for asset is already in a closed state  "+ currentState);
				return;
			}
			
			log.info(String.format("Import finished for asset %s (%s)", task.getAttributeAsString(Attribute.HOUSE_ID), peerID));
			AttributeMap updateMap = taskController.updateMapForTask(task);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
			updateMap.setAttribute(Attribute.INGEST_NOTES, "Unmatched asset matched to this placeholder");
			updateMap.setAttribute(Attribute.CONT_FMT, task.getAttribute(Attribute.REQ_FMT));
			taskController.saveTask(updateMap);
		}
		catch (MayamClientException e)
		{
			log.error("Error closing ingest task for asset " + peerID);
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
