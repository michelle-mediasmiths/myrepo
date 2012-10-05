package com.mediasmiths.activemq;

import java.util.Observable;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

public class ActiveMqListener extends Observable implements MessageListener, ExceptionListener
{	
	private static Logger logger = Logger.getLogger(ActiveMqClient.class);
	
	synchronized public void onException(JMSException ex) 
	{
		logger.error("JMS Exception caught in Active Mq Listener", ex);
	}
	
	public void onMessage(Message message) 
	{
		logger.info("JMS message received: " + message.toString());
		setChanged();
		notifyObservers(message);
	}
}
