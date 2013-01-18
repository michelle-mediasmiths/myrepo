package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
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
	

	protected boolean attributeChanged(Attribute att, AttributeMap before, AttributeMap after)
	{
		Object b = before.getAttribute(att);
		Object a = after.getAttribute(att);
		
		boolean inAfter = a!=null;
		boolean inBefore = b!=null;

		if (inAfter != inBefore)
		{
			return true;
		}
		else if (inAfter && inBefore)
		{	
			return before.getAttribute(att).equals(after.getAttribute(att));
		}
		else
		{
			return false;
		}

	}
}
