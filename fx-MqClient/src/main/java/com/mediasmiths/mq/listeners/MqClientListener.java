package com.mediasmiths.mq.listeners;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.Mq.Listener;
import com.mediasmiths.mq.handlers.AttributeHandler;
import com.mediasmiths.mq.handlers.PropertiesHandler;

public abstract class MqClientListener implements Listener
{
	protected final static Logger log = Logger.getLogger(MqClientListener.class);
	
	protected void passEventToHandler(AttributeHandler handler, AttributeMap messageAttributes){
		
		try{
			log.trace(String.format("passing event to handler: %s", handler.getName()));
			handler.process(messageAttributes);
		}
		catch(Exception e){
			log.error(String.format("exception in handler: %s", handler.getName()), e);
			//deliberatly doesnt rethrow exception, so as to not prevent other handlers from being called
		}
	}
	
	protected void passEventToHandler(PropertiesHandler handler, Map<String, String> messageProperties){
		
		try{
			log.trace(String.format("passing event to handler: %s", handler.getName()));
			handler.process(messageProperties);
		}
		catch(Exception e){
			log.error(String.format("exception in handler: %s", handler.getName()), e);
			//deliberatly doesnt rethrow exception, so as to not prevent other handlers from being called
		}
	}
}
