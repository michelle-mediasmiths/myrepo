package com.mediasmiths.mq.handlers.asset;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import org.apache.log4j.Logger;

public class MaterialAssociatedWithTitleHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(MaterialAssociatedWithTitleHandler.class);


	@Override
	public void process(final AttributeMap currentAttributes, final AttributeMap before, final AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = currentAttributes.getAttributeAsString(Attribute.ASSET_ID);
		String houseID = currentAttributes.getAttributeAsString(Attribute.HOUSE_ID);

		if (!MayamAssetType.MATERIAL.getAssetType().equals(assetType))
		{
			return; // only interested in materials
		}

		//if the parent id was null and now isn't
		if (before.getAttribute(Attribute.ASSET_PARENT_ID) == null && after.getAttribute(Attribute.ASSET_PARENT_ID) != null)
		{

			final String parentID = currentAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
			final String parentHouseID = currentAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);

			log.info(String.format("Item %s (%s) associated with %s (%s)", assetID, houseID, parentHouseID, parentID));

			AttributeMap title;
			try
			{
				title = titlecontroller.getTitleByArdomeId(parentID);
			}
			catch (MayamClientException e)
			{
				log.error("Failed to load title " + parentID, e);
				return;
			}

			//copy other metadata that the associated material should have as a result of moving to a new title
			log.info("Updating metadata that inherits from title");
			materialController.updateMaterialWithAttributesInheritedFromTitle(title,currentAttributes);
		}
	}


	@Override
	public String getName()
	{
		return "Material associated with title";
	}
}
