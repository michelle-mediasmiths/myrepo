package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.anyString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.PackageType;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;

public class MayamPackageControllerTest {

	MayamPackageController controller;
	TasksClient client;
	MqClient mqClient;
	
	public MayamPackageControllerTest() {
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
		controller = new MayamPackageController(client, mqClient);
	}
	
	@Test
	public void testCreatePackage() 
	{
		PackageType txPackage = mock(PackageType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testUpdatePackage() 
	{
		PackageType txPackage = mock(PackageType.class);
		ProgrammeMaterialType.Presentation.Package updatePackage = mock(ProgrammeMaterialType.Presentation.Package.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
	
	@Test
	public void testCreatePackageFailed() 
	{
		PackageType txPackage = mock(PackageType.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testUpdatePackageFailed() 
	{
		PackageType txPackage = mock(PackageType.class);
		ProgrammeMaterialType.Presentation.Package updatePackage = mock(ProgrammeMaterialType.Presentation.Package.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_CREATION_FAILED);
	}
	
	@Test
	public void testCreatePackageException() 
	{
		PackageType txPackage = mock(PackageType.class);

		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}
	
	@Test
	public void testUpdatePackageException() 
	{
		PackageType txPackage = mock(PackageType.class);
		ProgrammeMaterialType.Presentation.Package updatePackage = mock(ProgrammeMaterialType.Presentation.Package.class);
		
		try {
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(returnCode, MayamClientErrorCode.MAYAM_EXCEPTION);
	}

	@Test
	public void testCreateNullPackage() 
	{
		PackageType txPackage = null;
		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
		
	@Test
	public void testUpdateNullPackage() 
	{
		PackageType txPackage = null;
		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
		
		ProgrammeMaterialType.Presentation.Package updatePackage = null;
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(returnCode, MayamClientErrorCode.TITLE_UNAVAILABLE);
	}
	
	@Test
	public void testPackageExistsTrue() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(anyString());
		assertEquals(returnCode, true);
	}
	
	@Test
	public void testPackageExistsFalse() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testPackageExistsException() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(anyString());
		assertEquals(returnCode, false);
	}
	
	@Test
	public void testGetPackageValid() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackage(anyString());
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetPackageInValid() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackage(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void testGetPackageException() 
	{
		try {
			when(client.getAsset(AssetType.PACK, anyString())).thenThrow(new RemoteException(null, null));
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackage(anyString());
		assertEquals(attributes, null);
	}
	
	@Test
	public void deletePackage() 
	{
		MayamClientErrorCode returnCode = controller.deletePackage(anyString());
		assertEquals(returnCode, MayamClientErrorCode.NOT_IMPLEMENTED);
	}
}
