package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class DeleteButton extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(DeleteButton.class);
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		log.info(String.format("Delete Requested for asset %s of type %s",houseID,type.toString()));
		
		if(type==MayamAssetType.MATERIAL.getAssetType()){
			MayamClientErrorCode deleteMaterial = materialController.deleteMaterial(houseID);
		}
		else if(type==MayamAssetType.TITLE.getAssetType()){
			titlecontroller.purgeTitle(houseID);			
		}
		else if(type==MayamAssetType.PACKAGE.getAssetType()){		
			log.error("not implemented");
			//TODO: implement;
			throw new RuntimeException("manual package delete not implemented");
		}
		
	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.DELETE;
	}

	@Override
	public String getName()
	{
		return "Delete Button Click";
	}

}
