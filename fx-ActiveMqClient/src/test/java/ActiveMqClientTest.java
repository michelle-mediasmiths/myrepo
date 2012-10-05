import static org.junit.Assert.*;

import javax.jms.JMSException;

import org.junit.Test;

import com.mediasmiths.activemq.ActiveMqClient;
import com.mediasmiths.activemq.ActiveMqSession;


public class ActiveMqClientTest {

	@Test
	public void getSession() {
		try {
			ActiveMqClient client = new ActiveMqClient();
			assertTrue(client.newSession() instanceof ActiveMqSession);
			assertTrue(client.newSession(1) instanceof ActiveMqSession);
			client.shutdown();
		} catch (JMSException e) {
			fail();
		}
	}

}
