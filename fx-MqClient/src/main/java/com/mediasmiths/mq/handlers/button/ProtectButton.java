package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.mq.AttributeMessageBuilder;
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

		if (!AssetProperties.isPurgeProtected(messageAttributes))
		{
			log.info("Protecting asset "+messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
			updateMap.setAttribute(Attribute.ASSET_TYPE, messageAttributes.getAttribute(Attribute.ASSET_TYPE));
			updateMap.setAttribute(Attribute.ASSET_ID, messageAttributes.getAttribute(Attribute.ASSET_ID));
			updateMap.setAttribute(Attribute.PURGE_PROTECTED, Boolean.TRUE);
			try
			{
				tasksClient.assetApi().updateAsset(updateMap);
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
