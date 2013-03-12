package com.mediasmiths.mq.handlers.unmatched;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import com.mediasmiths.mq.transferqueue.TransferItem;
import com.mediasmiths.mq.transferqueue.TransferManager;
import com.mediasmiths.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UnmatchedTaskUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedTaskUpdateHandler.class);

	@Inject
	TransferManager transferManager;

	/**
	 * The maximum amount of time we're willing to wait for a transfer to succeed/fail
	 */
	@Inject(optional=true)
	@Named("UnmatchedTaskUpdateHandler.transferTimeout")
	Timeout transferTimeout = new Timeout(48, TimeUnit.HOURS);

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

				AttributeMap assetAttributes = tasksClient.assetApi().getAsset(AssetType.ITEM,
				                                                               currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID));

				String targetAsset = assetAttributes.getAttributeAsString(Attribute.SOURCE_HOUSE_ID);
				
				boolean isAssociated = AssetProperties.isMaterialAssociated(currentAttributes);

				if (targetAsset !=  null &&  targetAsset.length() != 0 && !isAssociated)
				{ // refuse to match it.
					currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
					currentAttributes.setAttribute(Attribute.ERROR_MSG, "Cannot match asset to placeholder as placeholder already has media attached");
					taskController.saveTask(currentAttributes);
					return;
				}
				removeUnmatchedAssetFromTaskLists(assetID);


				final String assetId = currentAttributes.getAttribute(Attribute.ASSET_ID).toString();
				final String assetPeerId = currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString();

				// We're only willing to wait this long until we assume the transfer has timed out
				Date timeout = transferTimeout.start().getDate();


				transferManager.add(new TransferItem(assetId, assetPeerId, timeout));
			}
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling unmatched task update Message : " + e.getMessage(), e);
		}

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

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}
}
