package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.anyString;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.QualityCheckEnumType;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeDescription;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.attributes.shared.type.AspectRatio;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.controllers.MayamPackageControllerTest.AttributeMatcher;
import com.mediasmiths.mayam.listeners.MqClient;

public class MayamMaterialControllerTest {

	MayamMaterialController controller;
	TasksClient client;
	MqClient mqClient;
	MaterialType material;
	ProgrammeMaterialType programmeMaterial;
	AttributeMap map;
	
	public MayamMaterialControllerTest() {
		super();
	}
	
	class AttributeMapMatcher extends ArgumentMatcher<AttributeMap> {
	     public boolean matches(Object attributes) {
	         return true;
	     }
	 }
	
	class AttributeMatcher extends ArgumentMatcher<Attribute> {
	     public boolean matches(Object attribute) {
	         return attribute instanceof Attribute;
	     }
	 }
	 
	@Before
	public void setup()
	{
		client = mock(TasksClient.class);
		mqClient = mock(MqClient.class);
		controller = new MayamMaterialController(client, mqClient);
		
		material = mock(MaterialType.class);
		when(material.getMaterialD()).thenReturn("");
		when(material.getQualityCheckTask()).thenReturn(QualityCheckEnumType.AUTOMATIC_ON_INGEST);
		when(material.getRequiredBy()).thenReturn(mock(XMLGregorianCalendar.class));
		when(material.getRequiredFormat()).thenReturn("");
		
		programmeMaterial = mock(ProgrammeMaterialType.class);
		when(programmeMaterial.getAspectRatio()).thenReturn(AspectRatio.TV_4_3.toString());
		when(programmeMaterial.getDuration()).thenReturn("");
		when(programmeMaterial.getFormat()).thenReturn("");
		
		map = mock(AttributeMap.class);
		map.injectHelpers(mock(AttributeValidator.class), mock(AttributeDescription.Producer.class));
		when(map.setAttributeFromString(argThat(new AttributeMatcher()), anyString())).thenReturn(map);
	}
	
	@Test
	public void testCreateMaterial() 
	{		
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testUpdateMaterial() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
		
		returnCode = controller.updateMaterial(programmeMaterial);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testCreateMaterialFailed() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_CREATION_FAILED, returnCode);
	}
	
	@Test
	public void testUpdateMaterialFailed() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, returnCode);
		
		returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, returnCode);
	}
	
	@Test
	public void testCreateMaterialException() 
	{	
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}
	
	@Test
	public void testUpdateMaterialException() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
		
		returnCode = controller.updateMaterial(programmeMaterial);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}

	@Test
	public void testCreateNullMaterial() 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_UNAVAILABLE, returnCode);
	}
		
	@Test
	public void testUpdateNullMaterial() 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_UNAVAILABLE, returnCode);
		
		ProgrammeMaterialType updateMaterial = null;
		returnCode = controller.updateMaterial(updateMaterial);
		assertEquals(MayamClientErrorCode.MATERIAL_UNAVAILABLE, returnCode);
	}
	
	@Test
	public void testMaterialExistsTrue() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(eq(anyString()));
		assertEquals(true, returnCode);
	}
	
	@Test
	public void testMaterialExistsFalse() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testMaterialExistsException() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testGetMaterialValid() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(eq(anyString()));
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetMaterialInValid() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(eq(anyString()));
		assertEquals(attributes, null);
	}
	
	@Test
	public void testGetMaterialException() 
	{
		try {
			when(client.getAsset(eq(AssetType.ITEM), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(eq(anyString()));
		assertEquals(attributes, null);
	}
	
	@Test
	public void deleteMaterial() 
	{
		MayamClientErrorCode returnCode = controller.deleteMaterial(anyString());
		assertEquals(returnCode, MayamClientErrorCode.NOT_IMPLEMENTED);
	}
}
