package com.mediasmiths.foxtel.placeholder.requestValidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageValidationResult;
import com.mediasmiths.mayam.MayamClientException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.Source;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

public class CreateTitleTest extends PlaceHolderMessageValidatorTest {

	private final static String NEW_TITLE = "NEW_TITLE";
	
	public CreateTitleTest() throws JAXBException, SAXException {
		super();		
	}

	@Test
	public void testValidCreateTitle() throws Exception{
		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);
		File temp = createTempXMLFile(pm,"validCreateTitle");
		
		//test that the generated placeholder message is valid
		assertEquals(PlaceHolderMessageValidationResult.IS_VALID,toTest.validateFile(temp.getAbsolutePath()));
	}
	
	private PlaceholderMessage buildCreateTitleRequestSingleLicence(String titleID) throws DatatypeConfigurationException{
		//build request
		XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN1st);
		XMLGregorianCalendar endDate =  DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN10th);
		
		TitleDescriptionType tdt = new TitleDescriptionType();
		tdt.setProgrammeTitle("PROGRAMMETITLE");
		
		LicensePeriodType licencePeriod = new LicensePeriodType();
		licencePeriod.setStartDate(startDate);
		licencePeriod.setEndDate(endDate);
	
		LicenseHolderType licenceHolder = new LicenseHolderType();
		licenceHolder.setOrganisationID("ORGID");
		licenceHolder.setOrganisationName("ORGNAME");
		
		ChannelType channelType = new ChannelType();
		channelType.setChannelTag("AAA");
		channelType.setChannelName("Channel Name");
		
		Channels channels = new Channels();
		channels.getChannel().add(channelType);
		
		License licence = new License();
		licence.setLicensePeriod(licencePeriod);
		licence.setLicenseHolder(licenceHolder);
		licence.setChannels(channels);
		
		RightsType rights = new RightsType();
		rights.getLicense().add(licence);
		
		CreateOrUpdateTitle coup = new CreateOrUpdateTitle();
		coup.setTitleID(titleID);
		coup.setTitleDescription(tdt);
		coup.setRights(rights);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(coup);
		
		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(MESSAGE_ID);
		pm.setSenderID(SENDER_ID);
		pm.setActions(actions);
		return pm;
}
}
