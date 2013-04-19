package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.AttributeHandler;
import com.mediasmiths.mq.transferqueue.TransferItem;
import com.mediasmiths.mq.transferqueue.UnmatchedTransferManager;
import com.mediasmiths.std.threading.Timeout;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MediaMoveHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(MediaMoveHandler.class);

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
	public void process(AttributeMap messageAttributes)
	{
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		
		if(assetType==null){
			log.debug("Asset Type is null");
			return;
		}
		
		String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);

		AttributeMap unmatchedTask = null;
		try {
			unmatchedTask = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.UNMATCHED_MEDIA, assetID);			
		} catch (MayamClientException e1) {
		}
		if (unmatchedTask == null)
		{
			try
			{
				AttributeMap assetAttributes = tasksClient.assetApi().getAsset(
						assetType,
						assetID);
				AttributeMap peerAttributes = tasksClient.assetApi().getAsset(
						assetType,
						messageAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID));
	
				String targetAsset = peerAttributes.getAttributeAsString(Attribute.SOURCE_HOUSE_ID);
				
				boolean isAssociated = AssetProperties.isMaterialAssociated(assetAttributes);
	
				if (targetAsset != null && targetAsset.length() != 0 && !isAssociated)
				{ 
					log.warn("Asset is not associated and has a target asset, exiting");
					return;
				}
	
				if (isAssociated)
				{
					// Content matched as associated media should not be added as a revision of the item.
					// It should be attached as a new associated item to the subprogram
					// If possible, the title of this media should be set to the filename,
					// so that a user can easily distinguish what is the programme asset and what is associated media by looking at its title within the item hierarchy.
					log.info("Match found for Associated asset, attempting to attach new associated item to subprogram");
					log.info("Match Peer: " + assetAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID) + " to " + targetAsset);
	
					AttributeMap associatedMaterial = materialController.getMaterialAttributes(assetAttributes.getAttributeAsString(Attribute.HOUSE_ID));
	
					associatedMaterial.setAttribute(
							Attribute.ASSET_PARENT_ID,
							peerAttributes.getAttributeAsString(Attribute.ASSET_PARENT_ID));
					associatedMaterial.setAttribute(
							Attribute.PARENT_HOUSE_ID,
							peerAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
					associatedMaterial.setAttribute(
							Attribute.SOURCE_HOUSE_ID,
							peerAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
	
					tasksClient.assetApi().updateAsset(associatedMaterial);
	
					String format = materialController.getFormat(associatedMaterial);
					log.debug(String.format("Format returned was %s; now closing task", format));
					setFormat(messageAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID), format);
				}
				else
				{
					log.info("Attempting to create new revision");
	
					final String unmatchedAssetID = messageAttributes.getAttribute(Attribute.ASSET_ID).toString();
					final String unmatchedAssetHouseID = assetAttributes.getAttribute(Attribute.HOUSE_ID).toString();
					final String assetPeerId = messageAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString();
					
					// copy metadata from unmatched asset to peer
					copyUnmatchedAttributes(unmatchedAssetID, assetPeerId);
	
					setContentFormatOnUmatchedAssetsPeer(unmatchedAssetID, assetPeerId);
	
					// We're only willing to wait this long until we assume the transfer has timed out
					Date timeout = transferTimeout.start().getDate();
					// queue transfer
					transferManager.add(new TransferItem(unmatchedAssetID,unmatchedAssetHouseID, assetPeerId, timeout));
				}
				
	
				List<AttributeMap> ingestTasks = taskController.getTasksForAsset(MayamTaskListType.INGEST, assetType, Attribute.ASSET_ID, assetID);
				for (AttributeMap ingestTask: ingestTasks)
				{
					ingestTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
					taskController.saveTask(ingestTask);
				}
					
				if (ingestTasks.size() > 1)
				{
					log.warn("More than 1 ingest task found for asset : " + assetID + ", " + ingestTasks.size() + "tasks found.");
				}
					
				List<AttributeMap> previewTasks = taskController.getTasksForAsset(MayamTaskListType.PREVIEW, assetType, Attribute.ASSET_ID, assetID);
				for (AttributeMap previewTask: previewTasks)
				{
					previewTask.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
					taskController.saveTask(previewTask);
				}
					
				if (previewTasks.size() > 1)
				{
					log.warn("More than 1 preview task found for asset : " + assetID + ", " + previewTasks.size() + "tasks found.");
				}
			}
			catch (Exception e)
			{
				log.error("Exception in the Mayam client while handling Media Move Message : " + e.getMessage(), e);
			}
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
				taskController.createWFEErorTask(
						MayamAssetType.MATERIAL,
						peerID,
						"Error determining content format during unmatched asset workflow");
			}
			catch (Exception e1)
			{
				log.error("error creating error task!", e1);
			}
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
}
