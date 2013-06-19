package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import com.mediasmiths.mq.handlers.purge.PurgeEvent;

public class PurgeProtectedUpdateHandler extends UpdateAttributeHandler
{
	@Inject
	PurgeEvent purgeEvent;

	@Override
	public void process(final AttributeMap currentAttributes, final AttributeMap before, final AttributeMap after)
	{
		if (attributeChanged(Attribute.PURGE_PROTECTED, before, after, currentAttributes))
		{
			purgeEvent.sendPurgeEventOnProtectedChange(currentAttributes);
		}
	}

	@Override
	public String getName()
	{
		return "Purge protected update";
	}
}
