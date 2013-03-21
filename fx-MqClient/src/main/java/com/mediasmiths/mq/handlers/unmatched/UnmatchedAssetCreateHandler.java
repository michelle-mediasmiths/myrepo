package com.mediasmiths.mq.handlers.unmatched;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mq.handlers.AttributeHandler;

public class UnmatchedAssetCreateHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedAssetCreateHandler.class);

	@Inject
	@Named("purge.unmatch.material.days")
	private int purgeTimeForUmatchedMaterial;

	public void process(AttributeMap messageAttributes)
	{

		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		// check this asset is an item\material
		if (!assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			log.info("Asset is not an ITEM, ignoring");
			return; // not interest in non items
		}

		String parentAssetID = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
		boolean unmatched = false;

		boolean noParent = false; // assume that items have a parent
		if (parentAssetID == null)
		{
			log.debug("Asset has not parent asset");
			noParent = true; // item does not have a parent, might be unmatched
		}
		else
		{
			log.debug("Assets parent is " + parentAssetID);
		}

		if (noParent)
		{
			// if item is created through DART then the user may enter a title id to use that the item to be saved to
			String userSelectedTitleID = messageAttributes.getAttribute(Attribute.AUX_VAL);
			log.debug("user selected title id from DART is " + userSelectedTitleID);

			if (userSelectedTitleID == null)
			{
				log.debug("no user selected title id, considering item to be unmatched");
				unmatched = true;
			}
			else
			{
				log.debug("user selected a title id attempting to assign item");
				if (assignToTitle(messageAttributes, userSelectedTitleID))
				{
					log.debug("asset assigned to user selected title");
				}
				else
				{
					log.debug("failed to assign to user selected title, treating content as unmatched");
					unmatched = true;
				}
			}
		}

		if (!unmatched)
		{
			log.debug("content not unmatched, returning");
			return;
		}

		try
		{
			log.info("unmatched asset created, adding to purge candidate list");

			// Add to purge candidate list with expiry date of 30 days
			String houseId = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			taskController.createOrUpdatePurgeCandidateTaskForAsset(
					MayamAssetType.MATERIAL,
					houseId,
					purgeTimeForUmatchedMaterial);
		}
		catch (Exception e)
		{
			log.error("Exception creating purge candidate task : ", e);
		}

		log.info("setting access rights on new unmatched asset");

		try
		{
			AttributeMap withNewAccessRights = accessRightsController.updateAccessRights(messageAttributes.copy());
			AttributeMap update = taskController.updateMapForAsset(withNewAccessRights);
			update.setAttribute(Attribute.ASSET_ACCESS, withNewAccessRights.getAttribute(Attribute.ASSET_ACCESS));
			tasksClient.assetApi().updateAsset(update);
		}
		catch (Exception e)
		{
			log.error("error setting assets access rights", e);
		}

	}

	private boolean assignToTitle(AttributeMap assetAttributes, String titleID)
	{
		log.debug("Attempting to associate with title "+titleID);
		
		AttributeMap title;
		try
		{
			title = titlecontroller.getTitle(titleID);
		}
		catch (MayamClientException e)
		{
			log.error(String.format("Could not find title with id %s", titleID), e);
			return false;
		}

		String titleAssetID = title.getAttribute(Attribute.ASSET_ID);

		AttributeMap updateMapForAsset = taskController.updateMapForAsset(assetAttributes);
		updateMapForAsset.setAttribute(Attribute.ASSET_PARENT_ID, titleAssetID);
		try
		{
			tasksClient.assetApi().updateAsset(updateMapForAsset);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Failed to set ASSET_PARENT_ID of asset to %s", titleAssetID), e);
			return false;
		}

		return true;

	}

	@Override
	public String getName()
	{
		return "Unmatched Asset Create";
	}
}
