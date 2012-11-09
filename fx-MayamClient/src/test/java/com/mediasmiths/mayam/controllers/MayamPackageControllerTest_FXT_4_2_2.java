package com.mediasmiths.mayam.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
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
import com.mayam.wf.ws.client.AssetApi;
import com.mayam.wf.ws.client.TaskApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MayamPackageControllerTest_FXT_4_2_2 {
	private static Logger logger = Logger.getLogger(MayamPackageControllerTest_FXT_4_2_2.class);

	MayamPackageController controller;
	TasksClient client;
	AssetApi assetApi;
	TaskApi taskApi;
	PackageType txPackage;
	AttributeMap map;
	ProgrammeMaterialType.Presentation.Package updatePackage;
	
	private final static String PACKAGEID="PACKAGEID";
	
	public MayamPackageControllerTest_FXT_4_2_2() {
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
	public void setup() throws DatatypeConfigurationException
	{
		client = mock(TasksClient.class);
		assetApi = mock(AssetApi.class);
		taskApi = mock(TaskApi.class);
		when(client.assetApi()).thenReturn(assetApi);
		when(client.taskApi()).thenReturn(taskApi);
		
		controller = new MayamPackageController(client, new DateUtil());
		
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
		
		logger.info("Starting FXT 4.2.2 Validate Functionality");

		try {
			when(client.createAttributeMap()).thenReturn(map);
			when(assetApi.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}

		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		
		
		if(MayamClientErrorCode.SUCCESS== returnCode)
		{
			logger.info(" FXT 4.2.2 Validate Functionality --Passed" );
		}
		else
		{
			logger.info(" FXT 4.2.2 Validate Functionality --Failed");
		}
		assertEquals(MayamClientErrorCode.SUCCESS, returnCode);
	}
	
	@Test
	public void testUpdatePackage() 
	{
		try {
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
			when(assetApi.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(map);
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
			when(assetApi.createAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
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
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
			when(assetApi.updateAsset(argThat(new AttributeMapMatcher()))).thenReturn(null);
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
		when(assetApi.createAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
		when(client.createAttributeMap()).thenReturn(map);
		MayamClientErrorCode returnCode = controller.createPackage(txPackage);
		assertEquals(MayamClientErrorCode.MAYAM_EXCEPTION, returnCode);
	}
	
	@Test
	public void testUpdatePackageException() throws Exception
	{
		when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(map);
		when(assetApi.updateAsset(argThat(new AttributeMapMatcher()))).thenThrow(mock(RemoteException.class));
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
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(PACKAGEID);
		assertEquals(true, returnCode);
	}
	
	@Test
	public void testPackageExistsFalse() 
	{
		try {
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		boolean returnCode = controller.packageExists(PACKAGEID);
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testPackageExistsException() throws Exception 
	{
		when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		boolean returnCode = controller.packageExists(PACKAGEID);
		assertEquals(false, returnCode);
	}
	
	@Test
	public void testGetPackageValid() throws MayamClientException 
	{
		try {
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(new AttributeMap());
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackageAttributes(PACKAGEID);
		assertTrue(attributes != null);
	}
	
	@Test
	public void testGetPackageInValid() throws MayamClientException 
	{
		try {
			when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenReturn(null);
		} catch (RemoteException e) {
			fail();
		}
		AttributeMap attributes = controller.getPackageAttributes(PACKAGEID);
		assertEquals(null, attributes);
	}
	
	@Test(expected = MayamClientException.class)
	public void testGetPackageException() throws Exception
	{
		when(assetApi.getAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString())).thenThrow(mock(RemoteException.class));
		AttributeMap attributes = controller.getPackageAttributes(PACKAGEID);		
	}
	
	@Test
	public void deletePackageFail() 
	{
		try {
			doThrow(mock(RemoteException.class)).when(assetApi).deleteAsset(eq(MayamAssetType.PACKAGE.getAssetType()), anyString());
		} catch (RemoteException e) {
			fail();
		}
		MayamClientErrorCode returnCode = controller.deletePackage("packageID");
		assertEquals(returnCode, MayamClientErrorCode.PACKAGE_DELETE_FAILED);
	}
	
	@Test
	public void deletePackageSuccess() 
	{
		MayamClientErrorCode returnCode = controller.deletePackage("packageID");
		assertEquals(returnCode, MayamClientErrorCode.SUCCESS);
	}
}
