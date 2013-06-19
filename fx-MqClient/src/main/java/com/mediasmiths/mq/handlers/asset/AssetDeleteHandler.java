package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mq.handlers.AttributeHandler;
import com.mediasmiths.mq.handlers.purge.PurgeEvent;

public class AssetDeleteHandler extends AttributeHandler
{
	@Inject
	PurgeEvent purgeEvent;

	@Override
	public void process(final AttributeMap messageAttributes)
	{
		purgeEvent.sendPurgeEventOnDelete(messageAttributes);
	}

	@Override
	public String getName()
	{
		return "Asset Delete";
	}
}
