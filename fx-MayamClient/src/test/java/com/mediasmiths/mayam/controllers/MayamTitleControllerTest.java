package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeDescription;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class MayamTitleControllerTest {

	MayamTitleController controller;
	TasksClient client;
	Material.Title title;
	CreateOrUpdateTitle cuTitle;
	TitleDescriptionType titleDescription;
	AttributeMap map;
	
	public MayamTitleControllerTest() {
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
		controller = new MayamTitleController(client);
		
		title = mock(Material.Title.class);		
		when(title.getTitleID()).thenReturn("");
		when(title.getProgrammeTitle()).thenReturn("");
		when(title.getSeriesNumber()).thenReturn(BigInteger.ZERO);
		when(title.getEpisodeTitle()).thenReturn("");
		when(title.getEpisodeNumber()).thenReturn(BigInteger.ZERO);
		when(title.getProductionNumber()).thenReturn("");
		when(title.getYearOfProduction()).thenReturn(BigInteger.ZERO);
		when(title.getCountryOfProduction()).thenReturn("");
		
		cuTitle = mock(CreateOrUpdateTitle.class);
		titleDescription = mock(TitleDescriptionType.class);
		when(cuTitle.getTitleDescription()).thenReturn(titleDescription);
		when(cuTitle.getTitleID()).thenReturn("");	
		when(cuTitle.isRestrictAccess()).thenReturn(false);	
		when(cuTitle.isPurgeProtect()).thenReturn(false);	
		when(titleDescription.getShow()).thenReturn("");	
		when(titleDescription.getProgrammeTitle()).thenReturn("");	
		when(titleDescription.getSeriesNumber()).thenReturn(BigInteger.ZERO);	
		when(titleDescription.getEpisodeTitle()).thenReturn("");	
		when(titleDescription.getEpisodeNumber()).thenReturn(BigInteger.ZERO);	
		when(titleDescription.getProductionNumber()).thenReturn("");	
		when(titleDescription.getStyle()).thenReturn("");	
		when(titleDescription.getYearOfProduction()).thenReturn(BigInteger.ZERO);	
		when(titleDescription.getCountryOfProduction()).thenReturn("");	
		
		map = mock(AttributeMap.class);
		map.injectHelpers(mock(AttributeValidator.class), mock(AttributeDescription.Producer.class));
		when(map.setAttributeFromString(argThat(new AttributeMatcher()), anyString())).thenReturn(map);
	}
	
	@Test
	public void testCreateTitle() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals( MayamClientErrorCode.SUCCESS, returnCode);
		
		returnCode = controller.createTitle(cuTitle);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testUpdateTitle() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(map);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
		
		returnCode = controller.updateTitle(cuTitle);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testCreateTitleFailed() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(MayamClientErrorCode.TITLE_CREATION_FAILED, returnCode);
		
		returnCode = controller.createTitle(cuTitle);
		assertEquals(MayamClientErrorCode.TITLE_CREATION_FAILED, returnCode);
	}
	
	@Test
	public void testUpdateTitleFailed() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(MayamClientErrorCode.TITLE_UPDATE_FAILED, returnCode);
		
		returnCode = controller.updateTitle(cuTitle);
		assertEquals(MayamClientErrorCode.TITLE_UPDATE_FAILED, returnCode);
	}
	
	@Test
	public void testCreateTitleException() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
		
		returnCode = controller.createTitle(cuTitle);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}
	
	@Test
	public void testUpdateTitleException() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
		
		returnCode = controller.updateTitle(cuTitle);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}

	@Test
	public void testCreateNullTitle() 
	{
		Material.Title title = null;
		MayamClientErrorCode returnCode = controller.createTitle(title);
		assertEquals(MayamClientErrorCode.TITLE_UNAVAILABLE, returnCode);
		
		CreateOrUpdateTitle newTitle = null;
		returnCode = controller.createTitle(newTitle);
		assertEquals(MayamClientErrorCode.TITLE_UNAVAILABLE, returnCode);
	}
	
	@Test
	public void testCreateTitleNullDescription() 
	{
		CreateOrUpdateTitle updateTitle = new CreateOrUpdateTitle();
		updateTitle.setTitleDescription(null);
		MayamClientErrorCode returnCode = controller.createTitle(updateTitle);
		assertEquals(MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE, returnCode);
	}
	
	@Test
	public void testUpdateNullTitle() 
	{
		Material.Title title = null;
		MayamClientErrorCode returnCode = controller.updateTitle(title);
		assertEquals(MayamClientErrorCode.TITLE_UNAVAILABLE, returnCode);
		
		CreateOrUpdateTitle updateTitle = null;
		returnCode = controller.updateTitle(updateTitle);
		assertEquals(MayamClientErrorCode.TITLE_UNAVAILABLE, returnCode);
	}
	
	@Test
	public void testUpdateTitleNullDescription() 
	{
		CreateOrUpdateTitle updateTitle = new CreateOrUpdateTitle();
		updateTitle.setTitleDescription(null);
		MayamClientErrorCode returnCode = controller.updateTitle(updateTitle);
		assertEquals(MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE, returnCode);
	}
	
	
	@Test
	public void testTitleExistsTrue() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(eq(anyString()));
		assertEquals(true, returnCode);
	}
	
	@Test
	public void testTitleExistsFalse() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testTitleExistsException() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.titleExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testGetTitleValid() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(eq(anyString()));
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetTitleInValid() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(eq(anyString()));
		assertEquals(null, attributes);
	}
	
	@Test
	public void testGetTitleException() 
	{
		try {
			when(client.getAsset(eq(AssetType.SER), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getTitle(eq(anyString()));
		assertEquals(null, attributes);
	}
	
	@Test
	public void deleteTitle() 
	{
		MayamClientErrorCode returnCode = controller.purgeTitle(mock(PurgeTitle.class));
		assertEquals(MayamClientErrorCode.NOT_IMPLEMENTED, returnCode);
	}
}
