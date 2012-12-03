package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.AttributeMap;

public class AssetDeletionHandler implements Handler 
{
	public AssetDeletionHandler() 
	{
		
	}
	
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
