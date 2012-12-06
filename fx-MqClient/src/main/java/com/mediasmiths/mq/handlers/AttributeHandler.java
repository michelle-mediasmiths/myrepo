package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.AttributeMap;

public interface AttributeHandler extends Handler
{
	public void process(AttributeMap messageAttributes) ;

	public String getName();
}
