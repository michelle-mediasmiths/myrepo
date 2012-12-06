package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.AttributeMap;

public class AssetPurgeHandler implements AttributeHandler
{
	public AssetPurgeHandler() 
	{
		
	}
	
	public void process(AttributeMap messageAttributes) 
	{
		//TODO: IMPLEMENT
		// - Purge of temporary assets notification received, remove from other worklist
		// - How to tell if an asset is ready to be purged? Check the Expiry date?
	}

	@Override
	public String getName()
	{
		return "Asset purge";
	}
}
