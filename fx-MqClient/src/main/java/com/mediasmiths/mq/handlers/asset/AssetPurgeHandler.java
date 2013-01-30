package com.mediasmiths.mq.handlers.asset;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mq.handlers.AttributeHandler;

public class AssetPurgeHandler extends AttributeHandler
{
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