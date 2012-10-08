package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.Library;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.QualityCheckEnumType;
import au.com.foxtel.cf.mam.pms.Source;
import au.com.foxtel.cf.mam.pms.TapeType;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.mayam.MayamClientErrorCode;

public class AddOrUpdateMaterialTest extends PlaceHolderMessageShortTest{
	
	public AddOrUpdateMaterialTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testAddMaterialProcessing () throws Exception {
		
		System.out.println("Add material processing");
		
		PlaceholderMessage message = buildAddMaterial(EXISTING_TITLE, NEW_MATERIAL_ID);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.materialExists(NEW_MATERIAL_ID)).thenReturn(false);
		when(mayamClient.createMaterial(aoum.getMaterial())).thenReturn(MayamClientErrorCode.SUCCESS);
		processor.processMessage(envelope);
		verify(mayamClient).createMaterial(aoum.getMaterial());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialXSDInvalid() throws Exception {
		
		System.out.println("FXT 4.1.5.2 - Non XSD compliance");
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write("InvalidAddMaterial", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testAddMaterialValidation() throws Exception {
		
		System.out.println("FXT 4.1.5.3/4/5 - XSD Compliance/ Valid AddOrUpdateMaterial message/ No matching ID exists");
		
		PlaceholderMessage message = buildAddMaterial(EXISTING_TITLE, NEW_MATERIAL_ID);
		File temp = createTempXMLFile(message, "validAddMaterial");
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		
		assertEquals(MessageValidationResult.IS_VALID, validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testUpdateMaterialProcessing() throws Exception {
		
		System.out.println("FXT 4.1.5.6 - Matching ID exists");
		PlaceholderMessage message = buildAddMaterial(EXISTING_TITLE, EXISTING_MATERIAL_ID);
		File temp = createTempXMLFile(message, "validUpdateMaterial");
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) message.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		
		when(mayamClient.materialExists(EXISTING_MATERIAL_ID)).thenReturn(new Boolean(true));
		when(mayamClient.updateMaterial(aoum.getMaterial())).thenReturn(MayamClientErrorCode.SUCCESS);
		processor.processMessage(envelope);
		verify(mayamClient).updateMaterial(aoum.getMaterial());
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testAddMaterialTitleDoesntExist() throws Exception {
		
		System.out.println("FXT 4.1.5.7 - No existing title");
		
		PlaceholderMessage message = buildAddMaterial(NOT_EXISTING_TITLE, NEW_MATERIAL_ID);
		File temp = createTempXMLFile (message, "addMaterialNoExistingTitle");
		
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);
		
		assertEquals(MessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL, validator.validateFile(temp.getAbsolutePath()));
	}
	
	protected PlaceholderMessage buildAddMaterial(String titleID, String materialID)
			throws DatatypeConfigurationException {
		return buildAddMaterialRequest(titleID, materialID, JAN1st, JAN10th);
	}
	
	protected static PlaceholderMessage buildAddMaterialRequest(String titleID, String materialID,
			GregorianCalendar created, GregorianCalendar required)
			throws DatatypeConfigurationException {
		MaterialType m = buildMaterial(materialID, created, required);

		AddOrUpdateMaterial aum = new AddOrUpdateMaterial();
		aum.setTitleID(titleID);
		aum.setMaterial(m);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				aum);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
	}
	
	protected static MaterialType buildMaterial(String materialID)
			throws DatatypeConfigurationException {
		return buildMaterial(materialID, JAN1st, JAN10th);
	}
	
	private static MaterialType buildMaterial(String materialID,
			GregorianCalendar created, GregorianCalendar required)
			throws DatatypeConfigurationException {
		// build request
		XMLGregorianCalendar orderCreated = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(created);
		XMLGregorianCalendar requiredBy = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(required);

		Order order = new Order();
		order.setOrderCreated(orderCreated);
		order.setOrderReference("abc123");
		
		Aggregator aggregator = new Aggregator();
		aggregator.setAggregatorID("def456");
		aggregator.setAggregatorName("aggregator");

		Aggregation aggregation = new Aggregation();
		aggregation.setOrder(order);
		aggregation.setAggregator(aggregator);
		
		/*Compile compile = new Compile();
		compile.setParentMaterialID("ghi789");*/
		
		TapeType tape = new TapeType();
		tape.setPresentationID("def456");
		tape.setLibraryID("ghi789");
		
		Library library = new Library();
		library.getTape().add(tape);
		
		Source s = new Source();
		s.setAggregation(aggregation);
		//s.setCompile(compile);
		//s.setLibrary(library);
		
		MaterialType m = new MaterialType();
		m.setMaterialD(materialID);
		m.setRequiredFormat("SD");
		QualityCheckEnumType qualityCheck = null;
		qualityCheck = qualityCheck.fromValue("AutomaticOnIngest");
		m.setQualityCheckTask(qualityCheck);
		m.setSource(s);
		m.setRequiredBy(requiredBy);
		
		return m;
	}
}
