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
		log.error("process(AttributeMap a) called on an UpdateAttributeHandler "+getName());
		process(messageAttributes, new AttributeMap(), new AttributeMap());
	}

	public abstract void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after);
	

	protected boolean attributeChanged(Attribute att, AttributeMap before, AttributeMap after, AttributeMap current)
	{
		Object b = before.getAttribute(att);
		Object c = current.getAttribute(att);
		
		boolean inCurrent = c!=null;
		boolean inBefore = b!=null;

		boolean ret = false;
		
		if (inCurrent != inBefore)
		{
			ret = true;
		}
		else if (inCurrent && inBefore)
		{	
			ret= ! b.equals(c);
		}
		else
		{
			ret = false;
		}

		return ret;
	}
}
