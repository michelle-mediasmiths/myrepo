package com.mediasmiths.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMqClient 
{
	protected ConnectionFactory connectionFactory;
	protected Connection connection;
			
	public ActiveMqClient() throws JMSException 
	{
		connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
		connection = connectionFactory.createConnection();
	    connection.start();
	}
		
	public void shutdown() throws JMSException {
		connection.close();
	}
		
	public ActiveMqSession newSession() throws JMSException
	{
		return new ActiveMqSession(connection);
	}
		
	public ActiveMqSession newSession(int sessionType) throws JMSException
	{
		return new ActiveMqSession(connection, sessionType);
	}
}
