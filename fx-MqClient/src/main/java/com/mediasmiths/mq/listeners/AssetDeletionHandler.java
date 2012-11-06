package com.mediasmiths.mq.listeners;

import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class AssetDeletionHandler 
{
	public static Listener getInstance(final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//TODO: IMPLEMENT
					// - Deletion has occurred in Viz Ardome, close all related workflow tasks - DG: Mayam or us?
					// - How to tell if an asset is deleted?
				}
			}
		};
	}
}
