package com.mediasmiths.mule;

import java.util.Map;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.client.DefaultLocalMuleClient;
import org.mule.config.builders.DefaultsConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;

public class MuleClientImpl implements IMuleClient {
	protected  MuleClient client = null;
	
	public MuleClientImpl() {
		
		//Create a MuleContextFactory
		MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();

		//create the configuration builder and optionally pass in one or more of these
		ConfigurationBuilder builder = new DefaultsConfigurationBuilder();
		//The actual context builder to use
		MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();

		//Create the context
		MuleContext context;
		try {
			context = muleContextFactory.createMuleContext(builder, contextBuilder);
			//Start the context
			context.start();
			//Create the client with the context
			client = new DefaultLocalMuleClient(context);
			
		} catch (MuleException e) {
			e.printStackTrace();
		}
	
	}

	public MuleMessage send(String destination, Object payLoad,  Map<String, Object> properties) {
		MuleMessage result = null;
		try {
			result = client.send(destination, payLoad, properties);
		} catch (MuleException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void dispatch(String destination, Object payLoad,  Map<String, Object> properties) {
		try {
			client.dispatch(destination, payLoad, properties);
		} catch (MuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public MuleMessage request(String destination, long port) {
		MuleMessage result = null;
		try {
			result = client.request(destination, port);
		} catch (MuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
