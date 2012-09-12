package com.mediasmiths.mayam.controllers;


import java.util.Date;
import org.junit.Before;
import org.junit.Test;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.ws.client.TasksClient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.anyObject;

import junit.framework.TestCase;

public class MayamAttributeControllerTest extends TestCase {
	
	MayamAttributeController controller;
	TasksClient client;
	AttributeValidator validator;
	
	public MayamAttributeControllerTest() {
		super();
	}
	
	@Before
	public void setup()
	{
		client = mock(TasksClient.class);
		validator = mock(AttributeValidator.class);
		when(client.createAttributeMap()).thenReturn(new AttributeMap());
	}
	
	@Test
	public void testSetAndGetInValidAttributeCreate() 
	{
		controller = new MayamAttributeController(client, validator);
		Date dateObject = new Date();
		when(validator.isValidValue(Attribute.ASSET_ID, anyObject())).thenReturn(false);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, dateObject);
		assertFalse(valid);
		AttributeMap emptyAttributes = controller.getAttributes();
		String assetID = emptyAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals(null, assetID);
	}
	
	@Test
	public void testSetAndGetValidAttributeCreate() 
	{
		controller = new MayamAttributeController(client, validator);
		when(validator.isValidValue(Attribute.ASSET_ID, anyObject())).thenReturn(true);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "12345");
		assertTrue(valid);
		AttributeMap newAttributes = controller.getAttributes();
		String assetID = newAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
	}
	
	@Test
	public void testSetAttributeUpdateValid() 
	{
		AttributeMap attributes = client.createAttributeMap();
		attributes.setAttribute(Attribute.ASSET_ID, "12345");
		controller = new MayamAttributeController(attributes, validator);
		AttributeMap existingAttributes = controller.getAttributes();
		String assetID = existingAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
		when(validator.isValidValue(Attribute.ASSET_ID, anyObject())).thenReturn(true);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "67890");
		assertTrue(valid);
		AttributeMap updatedAttributes = controller.getAttributes();
		assetID = updatedAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("67890", assetID);
	}
	
	@Test
	public void testSetAttributeUpdateInValid() 
	{
		AttributeMap attributes = client.createAttributeMap();
		attributes.setAttribute(Attribute.ASSET_ID, "12345");
		controller = new MayamAttributeController(attributes, validator);
		AttributeMap existingAttributes = controller.getAttributes();
		String assetID = existingAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
		when(validator.isValidValue(Attribute.ASSET_ID, anyObject())).thenReturn(false);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "67890");
		assertFalse(valid);
		AttributeMap updatedAttributes = controller.getAttributes();
		assetID = updatedAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
	}
}
