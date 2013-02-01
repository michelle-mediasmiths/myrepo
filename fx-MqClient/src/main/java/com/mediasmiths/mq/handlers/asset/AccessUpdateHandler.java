package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class AccessUpdateHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(AccessUpdateHandler.class);

	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		boolean anyChanged = false;

		for (Attribute a : MayamAccessRightsController.attributesAffectingAccessRights)
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
				AttributeMap withNewAccessRights = accessRightsController.updateAccessRights(currentAttributes.copy());
				
				AttributeMap update = tasksClient.createAttributeMap();
				//make the update as minimal as possible, we only want to be setting access rights here
				update.setAttribute(Attribute.ASSET_TYPE, withNewAccessRights.getAttribute(Attribute.ASSET_TYPE));
				update.setAttribute(Attribute.ASSET_ID, withNewAccessRights.getAttribute(Attribute.ASSET_ID));
				update.setAttribute(Attribute.ASSET_ACCESS, withNewAccessRights.getAttribute(Attribute.ASSET_ACCESS));
				
				tasksClient.assetApi().updateAsset(update);
			}
			catch (Exception e)
			{
				log.error("error updating assets access rights", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Temporary Content";
	}
}
