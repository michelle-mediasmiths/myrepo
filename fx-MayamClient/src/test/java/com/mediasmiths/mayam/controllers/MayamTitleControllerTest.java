package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;

public class MayamTitleControllerTest {

	MayamTitleController controller;
	TasksClient client;
	MqClient mqClient;
	
	public MayamTitleControllerTest() {
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
		controller = new MayamTitleController(client, mqClient);
	}
	
	@Test
	public void testCreateTitle() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle newTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
		
		returnCode = controller.createTitle(newTitle);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testUpdateTitle() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle updateTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
		
		returnCode = controller.updateTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testCreateTitleFailed() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle newTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
		
		returnCode = controller.createTitle(newTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testUpdateTitleFailed() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle updateTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
		
		returnCode = controller.updateTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testCreateTitleException() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle newTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
		
		returnCode = controller.createTitle(newTitle);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}
	
	@Test
	public void testUpdateTitleException() 
	{
		Material.Title title = mock(Material.Title.class);
		CreateOrUpdateTitle updateTitle = mock(CreateOrUpdateTitle.class);
		
		try {
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
		
		returnCode = controller.updateTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}

	@Test
	public void testCreateNullTitle() 
	{
		Material.Title title = null;
		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
		
		CreateOrUpdateTitle newTitle = null;
		returnCode = controller.createTitle(newTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
	
	@Test
	public void testCreateTitleNullDescription() 
	{
		CreateOrUpdateTitle updateTitle = new CreateOrUpdateTitle();
		updateTitle.setTitleDescription(null);
		MayamClientErrorCode returnCode = controller.createTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE);
	}
	
	@Test
	public void testUpdateNullTitle() 
	{
		Material.Title title = null;
		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
		
		CreateOrUpdateTitle updateTitle = null;
		returnCode = controller.updateTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
	
	@Test
	public void testUpdateTitleNullDescription() 
	{
		CreateOrUpdateTitle updateTitle = new CreateOrUpdateTitle();
		updateTitle.setTitleDescription(null);
		MayamClientErrorCode returnCode = controller.updateTitle(updateTitle);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE);
	}
	
	
	@Test
	public void testTitleExistsTrue() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(anyString());
		assertEquals(returnCode, true);
	}
	
	@Test
	public void testTitleExistsFalse() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testTitleExistsException() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testGetTitleValid() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(anyString());
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetTitleInValid() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void testGetTitleException() 
	{
		try {
			when(client.getAsset(AssetType.SER, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void deleteTitle() 
	{
		MayamClientErrorCode returnCode = controller.purgeTitle(mock(PurgeTitle.class));
		assertEquals(returnCode, MayamClientErrorCode.NOT_IMPLEMENTED);
	}
}
