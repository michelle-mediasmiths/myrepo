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

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeDescription;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeValidator;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class MayamPackageControllerTest {

	MayamPackageController controller;
	TasksClient client;
	PackageType txPackage;
	AttributeMap map;
	ProgrammeMaterialType.Presentation.Package updatePackage;
	
	public MayamPackageControllerTest() {
		super();
	}
	
	class AttributeMapMatcher extends ArgumentMatcher<AttributeMap> {
	     public boolean matches(Object attributes) {
	         return attributes instanceof AttributeMap;
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
		controller = new MayamPackageController(client);
		
		txPackage = mock(PackageType.class);
		when(txPackage.getClassification()).thenReturn(ClassificationEnumType.PG);
		when(txPackage.getPresentationFormat()).thenReturn(PresentationFormatType.HD);
		when(txPackage.getPresentationID()).thenReturn("");
		when(txPackage.getConsumerAdvice()).thenReturn("");
		when(txPackage.getMaterialID()).thenReturn("");
		when(txPackage.getNotes()).thenReturn("");
		when(txPackage.getNumberOfSegments()).thenReturn(BigInteger.ZERO);
		when(txPackage.getTargetDate()).thenReturn(mock(XMLGregorianCalendar.class));
		
		map = mock(AttributeMap.class);
		map.injectHelpers(mock(AttributeValidator.class), mock(AttributeDescription.Producer.class));
		when(map.setAttributeFromString(argThat(new AttributeMatcher()), anyString())).thenReturn(map);
		
		updatePackage = mock(ProgrammeMaterialType.Presentation.Package.class);
	}
	
	@Test
	public void testCreatePackage() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testUpdatePackage() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(map);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testCreatePackageFailed() 
	{
		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(client.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(MayamClientErrorCode.PACKAGE_CREATION_FAILED, returnCode);
	}
	
	@Test
	public void testUpdatePackageFailed() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
			when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(MayamClientErrorCode.PACKAGE_UPDATE_FAILED, returnCode);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(MayamClientErrorCode.PACKAGE_UPDATE_FAILED, returnCode);
	}
	
	@Test
	public void testCreatePackageException() throws Exception 
	{
		when(client.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		when(client.createAttributeMap()).thenReturn(map);
		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}
	
	@Test
	public void testUpdatePackageException() throws Exception
	{
		when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
		when(client.updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
		
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}

	@Test
	public void testCreateNullPackage() 
	{
		PackageType txPackage = null;
		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(MayamClientErrorCode.PACKAGE_UNAVAILABLE, returnCode);
	}
		
	@Test
	public void testUpdateNullPackage() 
	{
		PackageType txPackage = null;
		MayamClientErrorCode returnCode = controller.updatePackage(txPackage);
		assertEquals(MayamClientErrorCode.PACKAGE_UNAVAILABLE, returnCode);
		
		ProgrammeMaterialType.Presentation.Package updatePackage = null;
		returnCode = controller.updatePackage(updatePackage);
		assertEquals(MayamClientErrorCode.PACKAGE_UNAVAILABLE, returnCode);
	}
	
	@Test
	public void testPackageExistsTrue() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(eq(anyString()));
		assertEquals(true, returnCode);
	}
	
	@Test
	public void testPackageExistsFalse() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testPackageExistsException() throws Exception 
	{
		when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		boolean returnCode = controller.packageExists(eq(anyString()));
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testGetPackageValid() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackageAttributes(eq(anyString()));
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetPackageInValid() 
	{
		try {
			when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackageAttributes(eq(anyString()));
		assertEquals(null, attributes);
	}
	
	@Test
	public void testGetPackageException() throws Exception
	{
		when(client.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		AttributeMap attributes = controller.getPackageAttributes(eq(anyString()));
		assertEquals(null, attributes);
	}
	
	@Test
	public void deletePackage() 
	{
		MayamClientErrorCode returnCode = controller.deletePackage("Package ID");
		assertEquals(MayamClientErrorCode.NOT_IMPLEMENTED, returnCode);
	}
}
