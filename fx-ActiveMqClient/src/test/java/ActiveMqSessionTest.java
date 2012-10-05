import static org.junit.Assert.*;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;

import com.mediasmiths.activemq.ActiveMqErrorCode;
import com.mediasmiths.activemq.ActiveMqSession;


public class ActiveMqSessionTest {

	private ActiveMqSession session;
	private Connection connection;
	private Session mqSession;
	private Queue queue;
	private Topic topic;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private Serializable testObject;
	
	@Before
	public void setup() {
		connection = mock(Connection.class);
		mqSession = mock(Session.class);
		producer = mock(MessageProducer.class);
		consumer = mock(MessageConsumer.class);
		testObject = mock(Serializable.class);
		
		try {
			when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(mqSession);
			when(mqSession.createQueue(anyString())).thenReturn(queue);
			when(mqSession.createTopic(anyString())).thenReturn(topic);
			when(mqSession.createConsumer(queue)).thenReturn(consumer);
			when(mqSession.createConsumer(topic)).thenReturn(consumer);
			when(mqSession.createProducer(queue)).thenReturn(producer);
			when(mqSession.createProducer(topic)).thenReturn(producer);
			when(mqSession.createTextMessage(anyString())).thenReturn(mock(TextMessage.class));
			when(mqSession.createBytesMessage()).thenReturn(mock(BytesMessage.class));
			when(mqSession.createObjectMessage(testObject)).thenReturn(mock(ObjectMessage.class));
			session = new ActiveMqSession(connection);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestCreateQueue()
	{
		try {
			session.createQueue("QueueName");
			verify(mqSession).createQueue("QueueName");
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestCreateTopic()
	{
		try {
			session.createTopic("TopicName");
			verify(mqSession).createTopic("TopicName");
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestsSendTextSuccess()
	{
		try {
			session.createQueue("QueueName");
			ActiveMqErrorCode returnCode = session.send("QueueName", "Test String Message");
			assertEquals(ActiveMqErrorCode.SUCCESS, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestSendTextFail()
	{
		try {
			ActiveMqErrorCode returnCode = session.send("QueueName", "Test String Message");
			assertEquals(ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestSendBytesSuccess()
	{
		try {
			byte [] testBytes = {'a', 'b', 'c', 'd', 'e', 'f'};
			session.createQueue("QueueName");
			ActiveMqErrorCode returnCode = session.send("QueueName", testBytes);
			assertEquals(ActiveMqErrorCode.SUCCESS, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestSendBytesFail()
	{
		try {
			byte [] testBytes = {'a', 'b', 'c', 'd', 'e', 'f'};
			ActiveMqErrorCode returnCode = session.send("QueueName", testBytes);
			assertEquals(ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestSendObjectSuccess()
	{
		try {
			session.createQueue("QueueName");
			ActiveMqErrorCode returnCode = session.send("QueueName", testObject);
			assertEquals(ActiveMqErrorCode.SUCCESS, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestSendObjectFail()
	{
		try {
			ActiveMqErrorCode returnCode = session.send("QueueName", testObject);
			assertEquals(ActiveMqErrorCode.DESTINATION_DOES_NOT_EXIST, returnCode);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestReceiveSuccess()
	{
		try {
			Message expectedMessage = mock(Message.class);
			when(consumer.receive(1000)).thenReturn(expectedMessage);
			session.createQueue("QueueName");
			Message returnMessage = session.receive("QueueName");
			assertEquals(expectedMessage, returnMessage);
		} catch (JMSException e) {
			fail();
		}
	}
	
	@Test
	public void TestReceiveFail()
	{
		try {
			when(consumer.receive(1000)).thenReturn(null);
			session.createQueue("QueueName");
			Message returnMessage = session.receive("QueueName");
			assertEquals(null, returnMessage);
		} catch (JMSException e) {
			fail();
		}
	}

}
