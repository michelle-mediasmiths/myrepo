package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

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
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		if (attributeChanged(Attribute.PURGE_PROTECTED, before, after, currentAttributes))
		{
			log.debug("Purge protected atribute changed for material " + houseID);

			if (!AssetProperties.isPurgeProtected(currentAttributes))
			{
				log.debug("purge protected unset, returning");
				return;
			}

			log.debug("Material marked as purge protected, will attempt to mark title as purge protected if it isn't already");
			String parentAssetID = (String) currentAttributes.getAttribute(Attribute.ASSET_ID);
			String parentHouseID = (String) currentAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
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

	@Override
	public MayamAssetType handlesOnly()
	{
		return MayamAssetType.MATERIAL;
	}

}
