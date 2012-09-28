package com.mediasmiths.mule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleMessageCollection;
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
	private static Logger logger = Logger.getLogger(MuleClientImpl.class);
	
	public MuleClientImpl() throws MuleException {
		
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
			logger.error("Mule Exception caught when initiaiting Mule Client", e);
			throw e;
		}
	
	}
	
	public MuleClientImpl(MuleClient muleClient) {
		client = muleClient;
	}

	/* Example destination end points:
	 * 	- pop3://user:password@mymail.com
	 * 	- jms://message.queue
	 * 	- http://localhost/mule
	 * 	- file:///tmp/messages/in
	 * 	- vm://mypackage.myobject
	 * 	- axis:http://localhost/services/mypackage.myobject
	 */
	
	// Make a regular synchronous call
	public MuleMessage send(String destination, Object payLoad,  Map<String, Object> properties) {
		MuleMessage result = null;
		try {
			result = client.send(destination, payLoad, properties);
		} catch (MuleException e) {
			logger.error("Mule Exception caught when sending message to destination: " + destination);
		}
		return result;
	}

	// Make an asynchronous 'fire and forget' call
	public void dispatch(String destination, Object payLoad,  Map<String, Object> properties) {
		try {
			client.dispatch(destination, payLoad, properties);
		} catch (MuleException e) {
			logger.error("Mule Exception caught when dispatching message to destination: " + destination);
		}
	}
		
	public ArrayList<MuleMessage> request(String destination, long timeout) {
		MuleMessage result = null;
		try {
			result = client.request(destination, timeout);
		} catch (MuleException e) {
			logger.error("Mule Exception caught when requesting message from destination: " + destination);
		}
		ArrayList<MuleMessage> messages = null;
		if (result instanceof MuleMessageCollection)
		{
		    MuleMessageCollection resultsCollection = (MuleMessageCollection) result;
		    System.out.println("Number of messages: " + resultsCollection.size());
		    messages = new ArrayList<MuleMessage>(Arrays.asList(resultsCollection.getMessagesAsArray()));
		}
		else if (result != null) {
			messages = new ArrayList<MuleMessage>();
			messages.add(result);
		}
		return messages;
	}

}
