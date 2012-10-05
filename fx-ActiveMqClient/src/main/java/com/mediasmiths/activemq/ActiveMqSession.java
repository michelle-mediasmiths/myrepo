package com.mediasmiths.activemq;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Observer;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class ActiveMqSession {
	protected Connection connection;
	protected Session session;
	protected ActiveMqListener exceptionListener;
	
	protected HashMap<String, Destination> queueMap = new HashMap<String, Destination>();
	protected HashMap<String, Topic> topicMap = new HashMap<String, Topic>();
	protected HashMap<String, MessageConsumer> consumerMap = new HashMap<String, MessageConsumer>();
	protected HashMap<String, MessageProducer> producerMap = new HashMap<String, MessageProducer>();
	
	public ActiveMqSession(Connection activemqConnection) throws JMSException
	{
		connection = activemqConnection;
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		exceptionListener = new ActiveMqListener();
		connection.setExceptionListener(exceptionListener);
	}
	
	public ActiveMqSession(Connection activemqConnection, int sessionType) throws JMSException
	{
		connection = activemqConnection;
		session = connection.createSession(false, sessionType);
	}
	
	public void createQueue(String queueName) throws JMSException
	{
		Queue queue = session.createQueue(queueName);
		queueMap.put(queueName, queue);
		producerMap.put(queueName, session.createProducer(queue));
		consumerMap.put(queueName, session.createConsumer(queue));
	}
	
	public void createTopic(String topicName) throws JMSException
	{
		Topic topic = session.createTopic(topicName);
		topicMap.put(topicName, topic);
		producerMap.put(topicName, session.createProducer(topic));
		consumerMap.put(topicName, session.createConsumer(topic));
	}
	
	public ActiveMqErrorCode send(String destinationName, String textMessage) throws JMSException
	{
		ActiveMqErrorCode returnCode = ActiveMqErrorCode.SUCCESS;
		MessageProducer producer = producerMap.get(destinationName);
		if (producer != null) {
	        TextMessage message = session.createTextMessage(textMessage);
	        producer.send(message);
		} else {
			returnCode = ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST;
		}
        return returnCode;
	}
	
	public ActiveMqErrorCode send(String destinationName, byte [] byteMessage) throws JMSException
	{
		ActiveMqErrorCode returnCode = ActiveMqErrorCode.SUCCESS;
		MessageProducer producer = producerMap.get(destinationName);
		if (producer != null) {
			BytesMessage message = session.createBytesMessage();
			message.writeBytes(byteMessage);
	        producer.send(message);
		} else {
			returnCode = ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST;
		}
	    return returnCode;
	}
	
	public ActiveMqErrorCode send(String destinationName, Serializable objectMessage) throws JMSException
	{
		ActiveMqErrorCode returnCode = ActiveMqErrorCode.SUCCESS;
		MessageProducer producer = producerMap.get(destinationName);
		if (producer != null) {
			ObjectMessage message = session.createObjectMessage(objectMessage);
	        producer.send(message);
		} else {
			returnCode = ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST;
		}
	    return returnCode;
	}

	public Message receive(String destinationName, long timeout) throws JMSException
	{
		Message message = null;
		MessageConsumer consumer = consumerMap.get(destinationName);
		if (consumer != null) {
			message = consumer.receive(timeout);
		}
		return message;
	}
	
	public Message receive(String destinationName) throws JMSException
	{
		return receive(destinationName, 1000);
	}
	
	public void subscribe(String destinationName, Observer observer) throws JMSException
	{
		MessageConsumer consumer = consumerMap.get(destinationName);
		ActiveMqListener listener = new ActiveMqListener();
		consumer.setMessageListener(listener);
		listener.addObserver(observer);
	}
}
