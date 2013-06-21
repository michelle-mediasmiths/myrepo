package com.mediasmiths.mayam.controllers;


import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeDescription;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidationException;
import com.mayam.wf.attributes.shared.AttributeValidationException.FailureType;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MayamAttributeControllerTest {
	
	MayamAttributeController controller;
	TasksClientVeneer client;
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

		client = mock(TasksClientVeneer.class);
		when(client.createAttributeMap()).thenReturn(map);
		
		validator = mock(AttributeValidator.class);
	}
	
	@Test
	public void testSetAndGetInValidAttributeCreate() 
	{
		controller = new MayamAttributeController(client, validator);
		Date dateObject = new Date();
		Mockito.doThrow(new AttributeValidationException(FailureType.PATTERN_MISMATCH,"constraint","value")).when(validator).validate(eq(Attribute.ASSET_ID), anyObject());
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
//		when(validator.validate(eq(Attribute.ASSET_ID), anyObject())).thenReturn(true);
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
//		when(validator.validate(eq(Attribute.ASSET_ID), anyObject())).thenReturn(true);
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
		Mockito.doThrow(new AttributeValidationException(FailureType.PATTERN_MISMATCH,"constraint","value")).when(validator).validate(eq(Attribute.ASSET_ID), anyObject());
//		when(validator.validate(eq(Attribute.ASSET_ID), anyObject())).thenReturn(false);
		boolean valid = controller.setAttribute(Attribute.ASSET_ID, "67890");
		assertFalse(valid);
		AttributeMap updatedAttributes = controller.getAttributes();
		assetID = updatedAttributes.getAttribute(Attribute.ASSET_ID);
		assertEquals("12345", assetID);
	}
}
