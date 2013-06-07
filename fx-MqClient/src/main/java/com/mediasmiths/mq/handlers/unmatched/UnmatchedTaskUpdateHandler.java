package com.mediasmiths.mq.handlers.unmatched;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import com.mediasmiths.mq.transferqueue.TransferItem;
import com.mediasmiths.mq.transferqueue.UnmatchedTransferManager;
import com.mediasmiths.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UnmatchedTaskUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedTaskUpdateHandler.class);

	@Inject
	UnmatchedTransferManager transferManager;

	/**
	 * The maximum amount of time we're willing to wait for a transfer to succeed/fail
	 */
	@Inject(optional = true)
	@Named("UnmatchedTaskUpdateHandler.transferTimeout")
	Timeout transferTimeout = new Timeout(48, TimeUnit.HOURS);

	@Inject
	@Named("ff.sd.video.imagex")
	private int sdVideoX;

	@Override
	public String getName()
	{
		return "Unmatch Task Update";
	}

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
			
		boolean taskFailure = false;

		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		
		if(assetType==null){
			log.debug("task has been unassociated from its asset, ignoring");
			return;
		}
		
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		try
		{
			if (assetType.equals(MayamAssetType.MATERIAL.getAssetType())
					&& attributeChanged(Attribute.ASSET_PEER_ID, before, after, currentAttributes)
					&& currentAttributes.getAttribute(Attribute.ASSET_PEER_ID) != null)
			{

				log.info(String.format(
						"Unmatched task for material %s updated with match to asset %s",
						currentAttributes.getAttributeAsString(Attribute.HOUSE_ID),
						currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID)));

				AttributeMap peerAttributes = tasksClient.assetApi().getAsset(
						AssetType.ITEM,
						currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID));

				
				//check for AO mismatched
				//dont want to allow an ao item to be matched to non ao placholder item (or other way round)
				boolean itemIsAO = AssetProperties.isAO(currentAttributes);
				boolean peerIsAO = AssetProperties.isAO(peerAttributes); 
				
				if (itemIsAO != peerIsAO)
				{
					log.info(String.format("AO Mismatch between current item (%b) and selected peer (%b)", itemIsAO, peerIsAO));
					
					AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
					updateMap.setAttribute(Attribute.ASSET_PEER_ID, null); //clear the selected peer
					taskController.saveTask(updateMap);
					return;
				}

				String targetAsset = peerAttributes.getAttributeAsString(Attribute.SOURCE_HOUSE_ID);
				boolean peerIsComplianceItem = targetAsset != null && targetAsset.length() != 0;
				boolean peerIsPlaceholder = AssetProperties.isMaterialPlaceholder(peerAttributes);
				boolean unmatchedItemIsAssociatedContent = AssetProperties.isMaterialAssociated(currentAttributes);

				if (peerIsComplianceItem && !unmatchedItemIsAssociatedContent)
				{ // refuse to match it.
					clearPeerSetError(currentAttributes, "Cannot match asset to selected placeholder as it is a compliance item");
					return;
				}
				
				if(!peerIsPlaceholder && !unmatchedItemIsAssociatedContent){
					//refuse to match, selected item is not a placholder
					// refuse to match it.
					clearPeerSetError(currentAttributes, "Cannot match asset to selected item, it already has media");
					return;
				}

				if (unmatchedItemIsAssociatedContent)
				{
					// Content matched as associated media should not be added as a revision of the item.
					// It should be attached as a new associated item to the subprogram
					// If possible, the title of this media should be set to the filename,
					// so that a user can easily distinguish what is the programme asset and what is associated media by looking at its title within the item hierarchy.
					log.info("Match found for Associated asset, attempting to attach new associated item to subprogram");
					log.info("Match Peer: " + currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID) + " to "
							+ targetAsset);

					AttributeMap associatedMaterial = materialController.getMaterialAttributes(houseID);

					// MAM-196 Jonas states we need to use the target's ASSET_PARENT_ID and make the association with that.

					// this is the matching material - need to fetch its attributes to use its parent/parent house id
					String matchId = currentAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID);

					if (matchId == null)
					{
						taskFailure = true;
						log.debug("Peer id is null: unable to deduce parent");
						currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
						currentAttributes.setAttribute(Attribute.ERROR_MSG, "Cannot match asset as peer id is null");
						taskController.saveTask(currentAttributes);
						throw new Exception("Peer id is null: unable to deduce parent");
					}

					log.debug("Use Asset_Peer_ID= " + matchId + " (for the association)");

					AttributeMap materialMatch = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), matchId);

					if (materialMatch == null)
					{
						taskFailure = true;
						log.debug("Peer map is null: unable to deduce parent. Peer:" + matchId);
						currentAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
						currentAttributes.setAttribute(Attribute.ERROR_MSG, "Cannot match asset as unable to find peer asset "
								+ matchId + " to link to " + targetAsset);
						taskController.saveTask(currentAttributes);
						throw new Exception("unable to find peer asset " + matchId + " to link to " + targetAsset);
					}
					else
					{
						log.debug("Associate via parent: " + materialMatch.getAttributeAsString(Attribute.ASSET_PARENT_ID)
								+ " parentHouseId: " + materialMatch.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
					}

					// Terry - Update the attribute with the correct parent
					associatedMaterial.setAttribute(
							Attribute.ASSET_PARENT_ID,
							materialMatch.getAttributeAsString(Attribute.ASSET_PARENT_ID));
					associatedMaterial.setAttribute(
							Attribute.PARENT_HOUSE_ID,
							materialMatch.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
					associatedMaterial.setAttribute(
							Attribute.AUX_VAL,
							materialMatch.getAttributeAsString(Attribute.PARENT_HOUSE_ID));

					tasksClient.assetApi().updateAsset(associatedMaterial);

					String format = materialController.getFormat(associatedMaterial);
					log.debug(String.format("Format returned was %s; now closing task", format));

					// close unmatched task
					transferManager.closeTask(currentAttributes);

//					log.debug(String.format("PeerId returned %s, now setting format.", matchId));
//
//					// set the format (hd/sd, don't have a way of detecting 3d)
//					setFormat(matchId, format);
//
//					if (!taskFailure)
//					{
//						removeUnmatchedAssetFromTaskLists(houseID);
//						log.info("Unmatched task removed for asset : " + houseID);
//					}
				}
				else
				{
					log.info("Match found for asset, attempting to create new revision");

					final String unmatchedAssetID = currentAttributes.getAttribute(Attribute.ASSET_ID).toString();
					final String unmatchedAssetHouseID = currentAttributes.getAttribute(Attribute.HOUSE_ID).toString();
					final String assetPeerId = currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString();

					
					// copy metadata from unmatched asset to peer
					copyUnmatchedAttributes(unmatchedAssetID, assetPeerId);
					

					try
					{
						log.info("Trying to move task for transfer into SYS_WAIT state");
						List<AttributeMap> allUnmatchedTasks = taskController.getUmatchedTasksForItem(unmatchedAssetHouseID);
						setContentFormatOnUmatchedAssetsPeer(unmatchedAssetID, assetPeerId);

						// Now try to put the task(s) in SYS_WAIT
						for (AttributeMap task : allUnmatchedTasks)
						{
							AttributeMap updateMapForTask = taskController.updateMapForTask(task);
							updateMapForTask.setAttribute(Attribute.TASK_STATE, TaskState.SYS_WAIT);
							
							//MAM-297 mayam instruct us to clear the assocation from the unmatched task to the unmatched asset
							updateMapForTask.setAttribute(Attribute.ASSET_ID, null);
							updateMapForTask.setAttribute(Attribute.ASSET_PARENT_ID, null);
							updateMapForTask.setAttribute(Attribute.ASSET_TYPE, null);
													
							taskController.saveTask(updateMapForTask);
						}

					}
					catch (MayamClientException e)
					{
						log.error(
								"Mayam Exception thrown while updating state of unmatched task for asset " + unmatchedAssetID,
								e);
					}

					// We're only willing to wait this long until we assume the transfer has timed out
					Date timeout = transferTimeout.start().getDate();
					// queue transfer
					transferManager.add(new TransferItem(unmatchedAssetID,unmatchedAssetHouseID, assetPeerId, timeout));
				}
			}
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling unmatched task update Message : " + e.getMessage(), e);
		}

	}

	private void setContentFormatOnUmatchedAssetsPeer(final String unmatchedAssetID, final String assetPeerId)
	{
		// get the format of the unmatched asset
		String format = null;
		try
		{
			AttributeMap assetAttributes = tasksClient.assetApi().getAsset(
					MayamAssetType.MATERIAL.getAssetType(),
					unmatchedAssetID);
			// get the format (hd/sd, don't have a way of detecting 3d)
			format =materialController.getFormat(assetAttributes);
			log.debug(String.format("Format returned was %s;", format));
		}
		catch (RemoteException e)
		{
			log.error("Mayam Exception thrown while retrieving format information for asset " + unmatchedAssetID, e);
		}

		if (format != null)
		{
			// set format of the peer to match that of the unmatched item, if detected
			setFormat(assetPeerId, format);
		}

		// both getFormat and setFormat will create error tasks if they run into problems, but we dont let failure to set the content format prevent the match from occuring
	}

	public void setFormat(final String peerID, final String format)
	{
		log.debug(String.format("Setting format for peerId %s to %s", peerID, format));
		try
		{
			final AttributeMap peer = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), peerID);

			final AttributeMap updateMap = taskController.updateMapForAsset(peer);
			updateMap.setAttribute(Attribute.CONT_FMT, format);
			tasksClient.assetApi().updateAsset(updateMap);
		}
		catch (RemoteException e)
		{
			log.error("Error setting content format on asset " + peerID, e);

			try
			{
				taskController.createWFEErrorTask(MayamAssetType.MATERIAL,
				                                  peerID,
				                                  "Error determining content format during unmatched asset workflow");
			}
			catch (Exception e1)
			{
				log.error("error creating error task!", e1);
			}
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

					if (!MayamTaskController.END_STATES.contains(currentState))
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

	public static final EnumSet<Attribute> ATTRIBUTES_TO_IGNORE_ON_COPY_FROM_UMATCHED_ASSET_TO_PEER = EnumSet.of(
			Attribute.ASSET_ID,
			Attribute.ASSET_TYPE,
			Attribute.ASSET_PEER_ID);

	private void copyUnmatchedAttributes(String unmatchedAssetID, String peerAssetID)
	{
		log.debug(String.format("Copying unmatched attributes for assetId %s", peerAssetID));

		try
		{
			final AttributeMap asset = tasksClient.assetApi().getAsset(AssetType.ITEM, peerAssetID);
			final AttributeMap unmatchedAttributes = tasksClient.assetApi().getAsset(AssetType.ITEM, unmatchedAssetID);
			final AttributeMap updateMap = taskController.updateMapForAsset(asset);

			for (Attribute attribute : unmatchedAttributes.getAttributeSet())
			{
				if (!ATTRIBUTES_TO_IGNORE_ON_COPY_FROM_UMATCHED_ASSET_TO_PEER.contains(attribute))
				{ // dont want to set a peer id on our peer!
					if (asset.getAttribute(attribute) == null)
					{
						log.trace(String.format(
								"Asset does not have attribute value set for attribute %s; will be setting this.",
								attribute.toString()));
						updateMap.setAttribute(attribute, unmatchedAttributes.getAttribute(attribute));
					}
					else
					{
						log.trace(String.format(
								"Asset already has attribute set for attribute %s; not overwriting this.",
								attribute.toString()));
					}
				}
			}
			log.debug("Now updating the asset");
			tasksClient.assetApi().updateAsset(updateMap);
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while updating asset : " + peerAssetID, e);
		}
	}
	
	private void clearPeerSetError(AttributeMap currentAttributes, String errorMessage) throws MayamClientException{
		AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
		updateMap.setAttribute(Attribute.ASSET_PEER_ID, null); //clear the selected peer
		updateMap.setAttribute(
				Attribute.ERROR_MSG,
				errorMessage);
		taskController.saveTask(updateMap);
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}
}
