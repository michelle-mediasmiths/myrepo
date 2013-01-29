package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class MaterialUpdateHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(MaterialUpdateHandler.class);

	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String materialID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			if (attributeChanged(Attribute.PURGE_PROTECTED, before, after,currentAttributes) || attributeChanged(Attribute.CONT_RESTRICTED_MATERIAL, before, after,currentAttributes))
			{
				boolean isProtected = currentAttributes.getAttribute(Attribute.PURGE_PROTECTED);
				boolean isAO = currentAttributes.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);
				if (isAO) {
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, "R");	
				}
				else if (isProtected) {
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, "2");	
				}
				else {
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, "M");
				}
				try {
					tasksClient.assetApi().updateAsset(currentAttributes);
				} catch (RemoteException e) {
					log.error("Exception thrown by Mayam whille updating archive policy for material : " + materialID, e);
					e.printStackTrace();
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
