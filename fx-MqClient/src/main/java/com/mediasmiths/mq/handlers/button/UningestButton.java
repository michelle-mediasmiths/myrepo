package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;

public class UningestButton extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(UningestButton.class);
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		log.info(String.format("Uningest Requested for asset %s",houseID));
		
		if(type==MayamAssetType.MATERIAL.getAssetType()){
			try
			{
				materialController.uningest(messageAttributes);
			}
			catch (MayamClientException e)
			{
				log.error("error performing uningest for asset "+houseID,e);
			}
		}
		
	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.UNINGEST;
	}

	@Override
	public String getName()
	{
		return "Uningest Button Click";
	}

}
