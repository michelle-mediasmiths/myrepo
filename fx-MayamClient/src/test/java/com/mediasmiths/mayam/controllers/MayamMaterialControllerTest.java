package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.anyString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.MaterialType;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;

public class MayamMaterialControllerTest {

	MayamMaterialController controller;
	TasksClient client;
	MqClient mqClient;
	
	public MayamMaterialControllerTest() {
		super();
	}
	
	class AttributeMapMatcher extends ArgumentMatcher<AttributeMap> {
	     public boolean matches(Object attributes) {
	         return true;
	     }
	 }
	 
	@Before
	public void setup()
	{
		client = mock(TasksClient.class);
		mqClient = mock(MqClient.class);
		controller = new MayamMaterialController(client, mqClient);
	}
	
	@Test
	public void testCreateMaterial() 
	{
		MaterialType material = mock(MaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testUpdateMaterial() 
	{
		MaterialType material = mock(MaterialType.class);
		ProgrammeMaterialType updateMaterial = mock(ProgrammeMaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
		
		returnCode = controller.updateMaterial(updateMaterial);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testCreateMaterialFailed() 
	{
		MaterialType material = mock(MaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testUpdateMaterialFailed() 
	{
		MaterialType material = mock(MaterialType.class);
		ProgrammeMaterialType updateMaterial = mock(ProgrammeMaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
		
		returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testCreateMaterialException() 
	{
		MaterialType material = mock(MaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}
	
	@Test
	public void testUpdateMaterialException() 
	{
		MaterialType material = mock(MaterialType.class);
		ProgrammeMaterialType updateMaterial = mock(ProgrammeMaterialType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
		
		returnCode = controller.updateMaterial(updateMaterial);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}

	@Test
	public void testCreateNullMaterial() 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.createMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
		
	@Test
	public void testUpdateNullMaterial() 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
		
		ProgrammeMaterialType updateMaterial = null;
		returnCode = controller.updateMaterial(updateMaterial);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
	
	@Test
	public void testMaterialExistsTrue() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(anyString());
		assertEquals(returnCode, true);
	}
	
	@Test
	public void testMaterialExistsFalse() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testMaterialExistsException() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testGetMaterialValid() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(anyString());
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetMaterialInValid() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void testGetMaterialException() 
	{
		try {
			when(client.getAsset(AssetType.ITEM, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterial(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void deleteMaterial() 
	{
		MayamClientErrorCode returnCode = controller.deleteMaterial(anyString());
		assertEquals(returnCode, MayamClientErrorCode.NOT_IMPLEMENTED);
	}
}
