package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class MaterialProtectHandler extends UpdateAttributeHandler
{
	private static Logger log = Logger.getLogger(MaterialProtectHandler.class);

	@Override
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
		
		if(attributeChanged(Attribute.PURGE_PROTECTED, before, after, currentAttributes)){
			log.debug("Purge protected atribute changed for material "+houseID);
		}
	}

	@Override
	public String getName()
	{
		return "Material Protect";
	}

	@Override
	public MayamAssetType handlesOnly(){
		return MayamAssetType.MATERIAL;
	}
	
}
