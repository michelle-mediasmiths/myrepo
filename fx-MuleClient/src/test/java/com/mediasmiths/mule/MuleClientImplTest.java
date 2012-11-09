package com.mediasmiths.mule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleMessageCollection;
import org.mule.module.client.MuleClient;

public class MuleClientImplTest {
	MuleClientImpl client;
	HashMap<String, Object> props;
	MuleClient muleClient;
	
	public MuleClientImplTest() {
		super();
	}
	
	@Before 
	public void setup()
	{
		props = new HashMap<String, Object>();
		muleClient = mock(MuleClient.class);
		client = new MuleClientImpl(muleClient);
	}
	
	@Test
	public void testSendSuccess() {
		if (client == null) {
			fail("Client failed to initialise");	
		}
		
		MuleMessage expectedPayload = mock(MuleMessage.class);
		try {
			when(muleClient.send("testDestination", "testPackage", props)).thenReturn(expectedPayload);
		} catch (MuleException e) {
			fail("MuleException when mocking send call");
		}
		when(expectedPayload.getPayload()).thenReturn("Success");
		
		MuleMessage returnMessage = client.send("testDestination", "testPackage", props);
		
		Object payload = returnMessage.getPayload();
		assertEquals("Success", payload);
	}
	
	@Test
	public void testSendFail() {
		if (client == null) {
			fail("Client failed to initialise");	
		}
		
		MuleMessage expectedPayload = null;
		try {
			when(muleClient.send("testDestination", "testPackage", props)).thenReturn(expectedPayload);
		} catch (MuleException e) {
			fail("MuleException when mocking send call");
		}
		
		MuleMessage returnMessage = client.send("testDestination", "testPackage", props);
		assertEquals(expectedPayload, returnMessage);
	}

	@Test
	public void testDispatch() {
		if (client == null) {
			fail("Client failed to initialise");	
		}

		client.dispatch("testDestination", "testPackage", props);
		try {
			verify(muleClient).dispatch("testDestination", "testPackage", props);
		} catch (MuleException e) {
			fail("MuleException when mocking dispatch call");
		}
	}
	
	@Test
	public void testRequestSingleSuccess() {
		if (client == null) {
			fail("Client failed to initialise");	
		}
		
		MuleMessage expectedPayload = mock(MuleMessage.class);
		when(expectedPayload.getPayload()).thenReturn("Success");
		ArrayList<MuleMessage> expectedMessages = new ArrayList<MuleMessage>();
		expectedMessages.add(expectedPayload);
		
		try {
			when(muleClient.request("testDestination", 5000)).thenReturn(expectedPayload);
		} catch (MuleException e) {
			fail("MuleException when mocking send call");
		}
		
		ArrayList<MuleMessage> returnMessages = client.request("testDestination", 5000);
		
		assertEquals(1, returnMessages.size());
		assertTrue(expectedMessages.equals(returnMessages));
		assertEquals(expectedPayload, returnMessages.get(0));
		assertEquals("Success", returnMessages.get(0).getPayload());
		
	}
	
	@Test
	public void testRequestMultipleSuccess() {
		if (client == null) {
			fail("Client failed to initialise");	
		}
		
		MuleMessage expectedPayload1 = mock(MuleMessage.class);
		when(expectedPayload1.getPayload()).thenReturn("Success1");
		
		MuleMessage expectedPayload2 = mock(MuleMessage.class);
		when(expectedPayload2.getPayload()).thenReturn("Success2");
		
		MuleMessage expectedPayload3 = mock(MuleMessage.class);
		when(expectedPayload3.getPayload()).thenReturn("Success3");
		
		ArrayList<MuleMessage> expectedMessages = new ArrayList<MuleMessage>();
		expectedMessages.add(expectedPayload1);
		expectedMessages.add(expectedPayload2);
		expectedMessages.add(expectedPayload3);
		
		MuleMessage[] messageCollection = {expectedPayload1, expectedPayload2, expectedPayload3};
		
		MuleMessageCollection collection = mock(MuleMessageCollection.class);
		when(collection.getMessagesAsArray()).thenReturn(messageCollection);
		
		try {
			when(muleClient.request("testDestination", 5000)).thenReturn(collection);
		} catch (MuleException e) {
			fail("MuleException when mocking send call");
		}
		
		ArrayList<MuleMessage> returnMessages = client.request("testDestination", 5000);
		
		assertEquals(3, returnMessages.size());
		assertTrue(expectedMessages.equals(returnMessages));
		
		assertEquals(expectedPayload1, returnMessages.get(0));
		assertEquals(expectedPayload2, returnMessages.get(1));
		assertEquals(expectedPayload3, returnMessages.get(2));
		
		assertEquals("Success1", returnMessages.get(0).getPayload());
		assertEquals("Success2", returnMessages.get(1).getPayload());
		assertEquals("Success3", returnMessages.get(2).getPayload());
	}
	
	@Test
	public void testRequestFail() {
		if (client == null) {
			fail("Client failed to initialise");	
		}
		
		MuleMessage expectedPayload = null;
		try {
			when(muleClient.request("testDestination", 5000)).thenReturn(expectedPayload);
		} catch (MuleException e) {
			fail("MuleException when mocking send call");
		}
		
		ArrayList<MuleMessage> returnMessages = client.request("testDestination", 5000);
		assertEquals(null, returnMessages);
	}
	
}
