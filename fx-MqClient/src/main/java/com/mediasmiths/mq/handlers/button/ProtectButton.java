package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;

public class ProtectButton extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(ProtectButton.class);

	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{

		AssetType assetType = (AssetType) messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		AttributeMap titleAttributes;

		if (assetType.equals(MayamAssetType.TITLE.getAssetType()))
		{
			try
			{
			titleAttributes =  titlecontroller.getTitle(messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			}
			catch (MayamClientException e)
			{
				log.error("error handling Protect request for a TITLE, failed to load title", e);
				return;
			}
		}
		else if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			try
			{
				titleAttributes = titlecontroller.getTitle(messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
			}
			catch (MayamClientException e)
			{
				log.error("error handling Protect request for an ITEM, failed to load parent title", e);
				return;
			}
		}
		else
		{
			log.error("Protect button click on unexpected asset type asset id is "
					+ messageAttributes.getAttributeAsString(Attribute.ASSET_ID));
			return;
		}

		if ( ! AssetProperties.isPurgeProtected(titleAttributes))
		{
			titleAttributes.setAttribute(Attribute.PURGE_PROTECTED, true);
			try
			{
				tasksClient.assetApi().updateAsset(titleAttributes);
			}
			catch (RemoteException e)
			{
				log.error("error updating purge protected flag on asset", e);
			}
		}
		else
		{
			log.info("Asset is already protected");
		}

	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.PROTECTED;
	}

	@Override
	public String getName()
	{
		return "Protect button";
	}

}
