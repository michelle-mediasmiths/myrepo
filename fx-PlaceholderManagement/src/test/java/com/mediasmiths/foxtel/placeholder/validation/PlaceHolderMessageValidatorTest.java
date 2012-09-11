package com.mediasmiths.foxtel.placeholder.validation;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.Source;

import com.mediasmiths.foxtel.placeholder.messagecreation.FileWriter;
import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

public abstract class PlaceHolderMessageValidatorTest {

	protected MessageValidator toTest;
	protected MayamClient mayamClient = mock(MayamClient.class);

	protected final static String MESSAGE_ID = "123456asdfg";
	protected final static String SENDER_ID = "123456asdfg";

	protected final static String EXISTING_TITLE = "EXISTING";
	protected final static String NOT_EXISTING_TITLE = "NOT_EXISTING";
	protected final static String NEW_MATERIAL_ID = "NEW_MATERIAL";
	protected final static String EXISTING_MATERIAL_ID = "NEW_MATERIAL";
	protected final static String EXISTING_PACKAGE_ID = "NEW_MATERIAL";

	protected final static GregorianCalendar JAN1st = new GregorianCalendar(
			2000, 1, 1, 0, 0, 1);
	protected final static GregorianCalendar JAN10th = new GregorianCalendar(
			2000, 1, 10, 0, 0, 1);

	public PlaceHolderMessageValidatorTest() throws JAXBException, SAXException {

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		toTest = new MessageValidator(unmarhsaller, mayamClient);

	}

	protected File createTempXMLFile(PlaceholderMessage pm, String description)
			throws IOException, Exception {
		// marshall and write to file
		FileWriter writer = new FileWriter();
		File temp = File.createTempFile(description, ".xml");
		writer.writeObjectToFile(pm, temp.getAbsolutePath());
		return temp;
	}

	protected static PlaceholderMessage buildAddMaterialRequest(String titleID,
			GregorianCalendar created, GregorianCalendar required)
			throws DatatypeConfigurationException {
		MaterialType m = buildMaterial(NEW_MATERIAL_ID,created, required);

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

	protected static MaterialType buildMaterial(String materialID) throws DatatypeConfigurationException{
		return buildMaterial(materialID,JAN1st,JAN10th);
	}
	
	private static MaterialType buildMaterial(String materialID,GregorianCalendar created,
			GregorianCalendar required) throws DatatypeConfigurationException {
		// build request
		XMLGregorianCalendar orderCreated = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(created);
		XMLGregorianCalendar requiredBy = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(required);

		Order order = new Order();
		order.setOrderCreated(orderCreated);

		Aggregation aggregation = new Aggregation();
		aggregation.setOrder(order);

		Source s = new Source();
		s.setAggregation(aggregation);

		MaterialType m = new MaterialType();
		m.setMaterialD(materialID);
		m.setSource(s);
		m.setRequiredBy(requiredBy);
		return m;
	}

	protected PlaceholderMessage buildAddMaterialRequest(String titleID)
			throws DatatypeConfigurationException {
		return buildAddMaterialRequest(titleID, JAN1st, JAN10th);
	}

}
