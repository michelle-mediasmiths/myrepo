package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.type.Job;

public interface JobHandler extends Handler
{
	public void process(Job jobMessage) ;

	public String getName();
}
