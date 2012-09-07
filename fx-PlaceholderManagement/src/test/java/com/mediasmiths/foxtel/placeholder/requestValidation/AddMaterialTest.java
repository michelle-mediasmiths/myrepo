package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.Source;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.foxtel.placeholder.messagecreation.FileWriter;

public class AddMaterialTest extends PlaceHolderMessageValidatorTest {

	public AddMaterialTest() throws JAXBException, SAXException {
		super();
	}
	
	private final static String EXISTING_TITLE = "EXISTING";
	private final static String NOT_EXISTING_TITLE = "NOT_EXISTING";
	private final static String NEW_MATERIAL_ID = "NEW_MATERIAL";
	private final static String MESSAGE_ID = "123456asdfg";
	private final static String SENDER_ID = "123456asdfg";
	
	private final static GregorianCalendar ORDER_CREATED = new GregorianCalendar(2000, 1, 1, 0, 0, 1);
	private final static GregorianCalendar REQUIRED_BY = new GregorianCalendar(2000, 1, 10, 0, 0, 1);
	
	@Test
	public void testValidAddMaterial() throws Exception{

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		
		//marshall and write to file
		FileWriter writer = new FileWriter();
		File temp = File.createTempFile("addMaterialTest", ".xml");		
		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		
		//prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		
		//test that the generated placeholder message is valid
		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,toTest.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	public void testAddMaterialTitleDoesntExist() throws Exception{
		PlaceholderMessage pm = buildAddMaterialRequest(NOT_EXISTING_TITLE);
		
		//marshall and write to file
		FileWriter writer = new FileWriter();
		File temp = File.createTempFile("addMaterialTest", ".xml");		
		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		
		//prepare mock mayamClient
		when(mayamClient.titleExists(NOT_EXISTING_TITLE)).thenReturn(false);
		
		//test that the generated placeholder message is valid
		assertEquals(PlaceHolderMessageValidationResult.NO_EXISTING_TITLE_FOR_MATERIAL,toTest.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	public void testAddMaterialOrderCreatedAfterRequiredBy(){
		assertTrue(false);
	}
	
	@Test
	public void testAddMaterialTitleExistRequestFails() throws Exception{
		
	}

	private PlaceholderMessage buildAddMaterialRequest(String titleID)
			throws DatatypeConfigurationException {
		//build request
		XMLGregorianCalendar orderCreated = DatatypeFactory.newInstance().newXMLGregorianCalendar(ORDER_CREATED);
		XMLGregorianCalendar requiredBy =  DatatypeFactory.newInstance().newXMLGregorianCalendar(REQUIRED_BY);

		Order order = new Order();
		order.setOrderCreated(orderCreated);
		
		Aggregation aggregation = new Aggregation();
		aggregation.setOrder(order);
		
		Source s = new Source();
		s.setAggregation(aggregation);
		
		MaterialType m = new MaterialType();
		m.setMaterialD(NEW_MATERIAL_ID);
		m.setSource(s);
		m.setRequiredBy(requiredBy);

		AddOrUpdateMaterial aum = new AddOrUpdateMaterial();
		aum.setTitleID(titleID);
		aum.setMaterial(m);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(aum);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
	}
	
	

}
