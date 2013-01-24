package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class TitleUpdateHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(TitleUpdateHandler.class);

	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String titleID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		if (assetType.equals(MayamAssetType.TITLE.getAssetType()))
		{

			boolean anyChanged = false;

			for (Attribute a : MayamMaterialController.materialsAttributesInheritedFromTitle)
			{
				if (attributeChanged(a, before, after,currentAttributes))
				{
					anyChanged = true;
					break;
				}
			}

			if (anyChanged)
			{
				try
				{
					log.info("title attribute(s) that should inherit to their materials changed");
					materialController.updateMaterialAttributesFromTitle(currentAttributes);
				}
				catch (MayamClientException e)
				{
					log.error("error updating title's materials", e);
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Temporary Content";
	}
}
