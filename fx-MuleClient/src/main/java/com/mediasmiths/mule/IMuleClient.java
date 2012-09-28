package com.mediasmiths.mule;

import java.util.Map;

import org.mule.api.MuleMessage;

public interface IMuleClient {
	public MuleMessage send(String destination, Object payLoad,  Map<String, Object> properties);
	public void dispatch(String destination, Object payLoad,  Map<String, Object> properties);
	public MuleMessage[] request(String destination, long port);
}
