package com.mediasmiths.mayam.controllers;


import java.util.Date;
import org.junit.Before;
import org.junit.Test;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeDescription;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTitleControllerTest.AttributeMatcher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;

import junit.framework.TestCase;

public class MayamAttributeControllerTest {
	
	MayamAttributeController controller;
	TasksClient client;
	AttributeValidator validator;
	AttributeMap map;
	
	public MayamAttributeControllerTest() {
		super();
	}
	
	@Before 
	public void setup()
	{
		map = mock(AttributeMap.class);
		map.injectHelpers(mock(AttributeValidator.class), mock(AttributeDescription.Producer.class));

		client = mock(TasksClient.class);
		when(client.createAttributeMap()).thenReturn(map);
		
		validator = mock(AttributeValidator.class);
	}
	
	@Test
	public void testSetAndGetInValidAttributeCreate() 
	{
		controller = new MayamAttributeController(client, validator);
		Date dateObject = new Date();
		when(validator.isValidValue(eq(Attribute.ASSET_ID), anyObject())).thenReturn(false);
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
		when(validator.isValidValue(eq(Attribute.ASSET_ID), anyObject())).thenReturn(true);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "12345");
		when(map.getAttribute(eq(Attribute.ASSET_ID))).thenReturn("12345");
		assertTrue(valid);
		AttributeMap newAttributes = controller.getAttributes();
		String assetID = newAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
	}
	
	@Test
	public void testSetAttributeUpdateValid() 
	{
		map.setAttribute(Attribute.ASSET_ID, "12345");
		when(map.getAttribute(eq(Attribute.ASSET_ID))).thenReturn("12345");
		controller = new MayamAttributeController(map, validator);
		AttributeMap existingAttributes = controller.getAttributes();
		String assetID = existingAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
		when(validator.isValidValue(eq(Attribute.ASSET_ID), anyObject())).thenReturn(true);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "67890");
		when(map.getAttribute(eq(Attribute.ASSET_ID))).thenReturn("67890");
		assertTrue(valid);
		AttributeMap updatedAttributes = controller.getAttributes();
		assetID = updatedAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("67890", assetID);
	}
	
	@Test
	public void testSetAttributeUpdateInValid() 
	{
		map.setAttribute(Attribute.ASSET_ID, "12345");
		when(map.getAttribute(eq(Attribute.ASSET_ID))).thenReturn("12345");
		controller = new MayamAttributeController(map, validator);
		AttributeMap existingAttributes = controller.getAttributes();
		String assetID = existingAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
		when(validator.isValidValue(eq(Attribute.ASSET_ID), anyObject())).thenReturn(false);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "67890");
		assertFalse(valid);
		AttributeMap updatedAttributes = controller.getAttributes();
		assetID = updatedAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
	}
}
