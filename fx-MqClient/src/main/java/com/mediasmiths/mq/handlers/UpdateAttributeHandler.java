package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;

public abstract class UpdateAttributeHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UpdateAttributeHandler.class);
	
	@Override
	public final void process(AttributeMap messageAttributes)
	{
		log.warn("process(AttributeMap a) called on an UpdateAttributeHandler");
		process(messageAttributes, new AttributeMap(), new AttributeMap());
	}

	public abstract void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after);
	
}
