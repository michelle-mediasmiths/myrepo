package com.mediasmiths.mq.handlers.asset;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mq.handlers.AttributeHandler;

public class AssetDeletionHandler extends AttributeHandler 
{
	public void process(AttributeMap messageAttributes) 
	{
		//TODO: IMPLEMENT
		// - Deletion has occurred in Viz Ardome, close all related workflow tasks - DG: Mayam or us?
		// - How to tell if an asset is deleted?
	}

	@Override
	public String getName()
	{
		return "Asset deletion";
	}
}