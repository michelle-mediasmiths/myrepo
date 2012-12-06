package com.mediasmiths.mq.handlers;

import java.util.Map;

public interface PropertiesHandler extends Handler
{
	public void process(Map<String, String> messageAttributes) ;

	public String getName();
}
