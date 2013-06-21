package com.mediasmiths.mq.handlers.asset;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import org.apache.log4j.Logger;

/**
 * When an Item is marked as purge protected, ensures that its parent title is also protected 
 *
 */
public class MaterialProtectHandler extends UpdateAttributeHandler
{
	private static Logger log = Logger.getLogger(MaterialProtectHandler.class);

	@Override
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		
		if(! MayamAssetType.MATERIAL.getAssetType().equals(assetType)){
			return;
		}
		
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
		String assetID = currentAttributes.getAttribute(Attribute.ASSET_ID);

		if (attributeChanged(Attribute.PURGE_PROTECTED, before, after, currentAttributes))
		{
			log.debug("Purge protected atribute changed for material " + houseID);

			if (!AssetProperties.isPurgeProtected(currentAttributes))
			{
				log.debug("purge protected unset, returning");
				return;
			}

			log.info("Fetching materials attributes (parent information is not included in asset update message from mayam");
			AttributeMap materialAttributes;
			try
			{
				materialAttributes = materialController.getMaterialByAssetId(assetID);
			}
			catch (MayamClientException e)
			{
				log.fatal("error loading asset, was it recently deleted?"+assetID,e);//logs as fatal because this really shouldn't happen unless the asset was deleted while the message we are responding to was deleted
				return;
			}
			
			log.debug("Material marked as purge protected, will attempt to mark title as purge protected if it isn't already");
			String parentAssetID = (String) materialAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
			String parentHouseID = (String) materialAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
			log.debug(String.format("Parent asset is %s (%s), loading by ASSET_ID", parentHouseID, parentAssetID));

			if (parentAssetID == null)
			{
				log.debug("parent asset id is null, returning");
				return;
			}

			AttributeMap titleAttributes = null;
			try
			{
				titleAttributes = titlecontroller.getTitleByArdomeId(parentAssetID);
			}
			catch (MayamClientException e)
			{
				log.error("failed to find title " + parentAssetID, e);
			}

			if (titleAttributes == null)
			{
				log.warn("title doesnt exist :" + parentAssetID);
				return;
			}

			if (AssetProperties.isPurgeProtected(titleAttributes))
			{
				log.info("title is already purge protected, returning");
				return;
			}
			else
			{
				log.debug("setting PURGE_PROTECTED on title");
				AttributeMap updateMap = taskController.updateMapForAsset(titleAttributes);
				updateMap.setAttribute(Attribute.PURGE_PROTECTED, Boolean.TRUE);
				try
				{
					tasksClient.assetApi().updateAsset(updateMap);
				}
				catch (RemoteException e)
				{
					log.error("error setting purge protected flag on title", e);
				}
			}

		}
	}

	@Override
	public String getName()
	{
		return "Material Protect";
	}
}
