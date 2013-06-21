package com.mediasmiths.mule;

import org.apache.log4j.Logger;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleMessageCollection;
import org.mule.module.client.MuleClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MuleClientImpl implements IMuleClient {
	protected  MuleClient client = null;
	private static Logger logger = Logger.getLogger(MuleClientImpl.class);
	
	public MuleClientImpl() throws MuleException {
		MuleContext context;
		try {
			client = new MuleClient("muleclient-config.xml");
			context = client.getMuleContext();
			context.start();
		
		} catch (MuleException e) {
			logger.error("Mule Exception caught when initiaiting Mule Client :"+ e, e);
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
			logger.error("Mule Exception: " + e);
		}
		return result;
	}

	// Make an asynchronous 'fire and forget' call
	public void dispatch(String destination, Object payLoad,  Map<String, Object> properties)  throws MuleException{
		client.dispatch(destination, payLoad, properties);
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
		    messages = new ArrayList<MuleMessage>(Arrays.asList(resultsCollection.getMessagesAsArray()));
		}
		else if (result != null) {
			messages = new ArrayList<MuleMessage>();
			messages.add(result);
		}
		return messages;
	}

}
