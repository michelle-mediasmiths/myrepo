package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Ignore;
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
import com.mayam.wf.ws.client.AssetApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MayamMaterialControllerTest {

	MayamMaterialController controller;
	TasksClient client;
	AssetApi assetApi;
	MaterialType material;
	ProgrammeMaterialType programmeMaterial;
	AttributeMap map;
	MayamTaskController taskController;
	private final static String MATERIALID="MATERIALID";
	private final static String TITLEID="TITLEID";
	
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
	public void setup() throws DatatypeConfigurationException
	{
		client = mock(TasksClient.class);
		assetApi = mock(AssetApi.class);
		taskController = mock(MayamTaskController.class);
		when(client.assetApi()).thenReturn(assetApi);
		controller = new MayamMaterialController(client, taskController);
		
		material = mock(MaterialType.class);
		when(material.getMaterialID()).thenReturn("PLACEHOLDER_MATERIAL");
		when(material.getQualityCheckTask()).thenReturn(QualityCheckEnumType.AUTOMATIC_ON_INGEST);
		when(material.getRequiredBy()).thenReturn(mock(XMLGregorianCalendar.class));
		when(material.getRequiredFormat()).thenReturn("");
		
		programmeMaterial = mock(ProgrammeMaterialType.class);
		when(programmeMaterial.getAspectRatio()).thenReturn(AspectRatio.TV_4_3.toString());
		when(programmeMaterial.getDuration()).thenReturn("");
		when(programmeMaterial.getFormat()).thenReturn("");
		
		map = mock(AttributeMap.class);
		map.injectHelpers(mock(AttributeValidator.class), mock(AttributeDescription.Producer.class));
		when(map.getAttribute(eq(Attribute.ASSET_ID))).thenReturn("123456776544321");
		when(map.getAttribute(eq(Attribute.ASSET_TYPE))).thenReturn(AssetType.ITEM);
		when(map.setAttributeFromString(argThat(new AttributeMatcher()), anyString())).thenReturn(map);
		
		when(client.createAttributeMap()).thenReturn(new AttributeMap());
	}
	
	@Test
	public void testCreateMaterial() 
	{		
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(assetApi.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material,TITLEID);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testUpdateMaterial() throws MayamClientException 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(map);
			when(assetApi.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
			
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
		
		Title title= new Title();
		title.setTitleID(TITLEID);
		Details details = new Details();
		
		controller.updateMaterial(programmeMaterial, details, title);		
	}
	
	@Test
	public void testCreateMaterialFailed() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(assetApi.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material,TITLEID);
		assertEquals(MayamClientErrorCode.MATERIAL_CREATION_FAILED, returnCode);
	}
	
	@Test
	public void testUpdateMaterialFailed() 
	{
		try {
			when(client.assetApi().getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(map);
			when(client.assetApi().createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
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
			when(client.assetApi().createAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createMaterial(material,TITLEID);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}
	
	@Test
	public void testUpdateMaterialException() throws MayamClientException 
	{
		try {
			when(client.assetApi().getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(map);
			when(client.assetApi().updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);	
	}

	@Test
	public void testCreateNullMaterial() 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.createMaterial(material,TITLEID);
		assertEquals(MayamClientErrorCode.MATERIAL_UNAVAILABLE, returnCode);
	}
		
	@Test(expected = MayamClientException.class)
	public void testUpdateNullMaterial() throws MayamClientException 
	{
		MaterialType material = null;
		MayamClientErrorCode returnCode = controller.updateMaterial(material);
		assertEquals(MayamClientErrorCode.MATERIAL_UNAVAILABLE, returnCode);
		
		ProgrammeMaterialType updateMaterial = null;
		Details details=null;
		Material.Title title = null;
		controller.updateMaterial(updateMaterial, details , title);
	}
	
	@Test
	public void testMaterialExistsTrue() 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(MATERIALID);
		assertEquals(true, returnCode);
	}
	
	@Test
	public void testMaterialExistsFalse() 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(MATERIALID);
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testMaterialExistsException() 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.materialExists(MATERIALID);
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testGetMaterialValid() 
	{
		try {
			when(client.assetApi().getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterialAttributes(MATERIALID);
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetMaterialInValid() 
	{
		try {
			when(client.assetApi().getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterialAttributes(MATERIALID);
		assertEquals(attributes, null);
	}
	
	@Test
	public void testGetMaterialException() 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getMaterialAttributes(MATERIALID);
		assertEquals(attributes, null);
	}
	
	@Test
	@Ignore //needs more to account for deleting packages beneath the material
	public void deleteMaterialFail() 
	{
		try {
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(map);
			doThrow(mock(RemoteException.class)).when(assetApi).deleteAsset(eq(MayamAssetType.MATERIAL.getAssetType()), anyString());
		} catch (RemoteException e) {
			fail();
		}
		MayamClientErrorCode returnCode = controller.deleteMaterial("materialID",0);
		assertEquals(returnCode, MayamClientErrorCode.MATERIAL_DELETE_FAILED);
	}
	
	@Test
	@Ignore //needs more to account for deleting packages beneath the material
	public void deleteMaterialSuccess() 
	{
		try
		{
			when(assetApi.getAssetBySiteId(eq(MayamAssetType.MATERIAL.getAssetType()), anyString())).thenReturn(map);
		}
		catch (RemoteException e)
		{
			fail();
		}
		MayamClientErrorCode returnCode = controller.deleteMaterial(MATERIALID,0);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
}
