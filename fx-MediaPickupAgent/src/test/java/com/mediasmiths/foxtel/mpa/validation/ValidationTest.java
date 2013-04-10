package com.mediasmiths.foxtel.mpa.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public abstract class ValidationTest {

	protected MaterialExchangeValidator validator;
	protected MayamClient mayamClient= mock(MayamClient.class);
	protected String receiptFolderPath;
	
	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String NOT_EXISTING_TITLE = "NOT_EXISTING_TITLE";
	protected final static String EXISTING_TITLE_CHECK_FAILS = "ETCE";
		
	protected final static String EXISTING_MATERIAL_NOT_PLACEHOLDER = "EXISTING_MATERIAL_NOT_PH";
	protected final static String EXISTING_MATERIAL_IS_PLACEHOLDER = "EXISTING_MATERIAL_IS_PLACEHOLDER";
	protected final static String NOT_EXISTING_MATERIAL = "NOT_EXISTING_MATERIAL";
	protected final static String EXISTING_MATERIAL_CHECK_FAILS = "EMCF";
	protected final static String IS_PLACEHOLDER_CHECK_FAILS = "IPCH";
	
	protected final static String EXISTING_PACKAGE = "EXISTING_PACKAGE";
	protected final static String EXISTING_PACKAGE2 = "EXISTING_PACKAGE2";
	protected final static String NOT_EXISTING_PACKAGE = "NOT_EXISTING_PACKAGE";
	protected final static String EXISTING_PACKAGE_CHECK_FAILS = "EPCF";
	
	@Before
	public void beforeClass() throws JAXBException, IOException, SAXException{
		JAXBContext jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		receiptFolderPath = TestUtil.prepareTempFolder("RECEIPTS");
		validator = new MaterialExchangeValidator(unmarhsaller, mayamClient, new ReceiptWriter(), new SchemaValidator("MaterialExchange_V2.0.xsd"));
		validator.setWatchFolders(new WatchFolders());
		validator.setAoAggregatorName("ADULTS");
		
	}
	
	protected MessageValidationResult validationForMaterial(Material material) throws Exception{
	
		String importFolderPath =  TestUtil.prepareTempFolder("INCOMING");
		String messageXmlPath = importFolderPath+ IOUtils.DIR_SEPARATOR +"MessageValidationResult"+ RandomStringUtils.randomAlphabetic(10) + FilenameUtils.EXTENSION_SEPARATOR + "xml"; 
		TestUtil.writeMaterialToFile(material,messageXmlPath);		
		prepareMocks();
		
		PickupPackage pp = new PickupPackage("xml", "mxf");
		pp.addPickUp(new File(messageXmlPath));
		
		return validator.validatePickupPackage(pp).getResult();
	}
	
	protected void prepareMocks() throws MayamClientException{
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);
		when(mayamClient.titleExists(EXISTING_TITLE_CHECK_FAILS)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		when(mayamClient.materialExists(EXISTING_MATERIAL_IS_PLACEHOLDER)).thenReturn(true);
		when(mayamClient.materialExists(EXISTING_MATERIAL_NOT_PLACEHOLDER)).thenReturn(true);
		when(mayamClient.materialExists(NOT_EXISTING_MATERIAL)).thenReturn(false);
		when(mayamClient.materialExists(IS_PLACEHOLDER_CHECK_FAILS)).thenReturn(true);
		when(mayamClient.materialExists(EXISTING_MATERIAL_CHECK_FAILS)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		
		when(mayamClient.isMaterialPlaceholder(EXISTING_MATERIAL_IS_PLACEHOLDER)).thenReturn(true);
		when(mayamClient.isMaterialPlaceholder(EXISTING_MATERIAL_NOT_PLACEHOLDER)).thenReturn(false);
		when(mayamClient.isMaterialPlaceholder(IS_PLACEHOLDER_CHECK_FAILS)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
		when(mayamClient.materialHasPassedPreview(anyString())).thenReturn(false);
		when(mayamClient.getLastDeliveryVersionForMaterial(anyString())).thenReturn(-1);
		
		
		when(mayamClient.packageExists(EXISTING_PACKAGE)).thenReturn(true);
		when(mayamClient.packageExists(EXISTING_PACKAGE2)).thenReturn(true);
		when(mayamClient.packageExists(NOT_EXISTING_PACKAGE)).thenReturn(false);
		when(mayamClient.packageExists(EXISTING_PACKAGE_CHECK_FAILS)).thenThrow(new MayamClientException(MayamClientErrorCode.FAILURE));
	}
		
}
