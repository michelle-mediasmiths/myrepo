package com.mediasmiths.foxtel.messagetests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.MSTitleDescription;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;


public class CreateOrUpdateTitleTest_FXT_4_1_1 extends PlaceHolderMessageShortTest {
	
	private final static String NEW_TITLE = "NEW_TITLE";
	private static Logger logger = Logger.getLogger(CreateOrUpdateTitleTest_FXT_4_1_1.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);


	public CreateOrUpdateTitleTest_FXT_4_1_1() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidCreateTitleProcessing() throws Exception {
		
		logger.info("Processing validation");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		CreateOrUpdateTitle coup = (CreateOrUpdateTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);
		when(mayamClient.titleExists(NEW_TITLE)).thenReturn(new Boolean(false));
		when(mayamClient.createTitle(coup)).thenReturn(MayamClientErrorCode.SUCCESS);
		
		processor.processMessage(envelope);
		verify(mayamClient).createTitle(coup);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testCreateTitleXSDInvalid_FXT_4_1_1_2() throws Exception {
		
		logger.info("Starting FXT 4.1.1.2 - Non XSD compliance");
		//File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		File temp = new File("/tmp/placeHolderTestData/NonXSDConformingFile__"+RandomStringUtils.randomAlphabetic(6)+ ".xml");

		IOUtils.write("InvalidCreateTitle", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.FAILS_XSD_CHECK ==validateFile)
			resultLogger.info("FXT 4.1.1.2 - Non XSD compliance --Passed");
		else
			resultLogger.info("FXT 4.1.1.2 - Non XSD compliance --Failed");
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
		
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testValidCreateTitle_FXT_4_1_1_3_4() throws Exception {
		
		logger.info("Starting FXT 4.1.1.3/4 - XSD Compliance/ Non-existing ID");
		logger.info("Starting FXT 4.1.0.3 – Valid CreateOrUpdateTitle Message");

		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		File temp = createTempXMLFile(message, "validCreateTitle");

		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.IS_VALID ==validateFile)
		{
			resultLogger.info("FXT 4.1.1.3/4 - XSD Compliance/ Non-existing ID --Passed");
			resultLogger.info("FXT 4.1.0.3 – Valid CreateOrUpdateTitle Message --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.1.1.3/4 - XSD Compliance/ Non-existing ID --Failed");
			resultLogger.info("FXT 4.1.0.3 – Valid CreateOrUpdateTitle Message --Failed");
		}
		
		assertEquals(MessageValidationResult.IS_VALID, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidCreateTitleProcessingFailsOnExistingTitle() throws Exception {
		
		logger.info("Starting FXT 4.1.25  - CreateorUpdateTitle error handling");
		PlaceholderMessage message = buildCreateTitle(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenThrow(
				
				new MayamClientException(MayamClientErrorCode.FAILURE));
		try
		{
		processor.processMessage(envelope);
		}
		catch (MessageProcessingFailedException e)
		{
			resultLogger.info("FXT 4.1.25  - CreateOrUpdateTitle error handling --Passed");
			throw e;
		}
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testCreateTitleInvalidDates_FXT_4_1_0_4_5_FXT_4_1_1_6() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.0.4/5 - Invalid license dates");
		logger.info("Starting FXT 4.1.1.6 - Invalid license dates");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		
		List<License> license = ((CreateOrUpdateTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0)).getRights().getLicense();
		
		XMLGregorianCalendar startDate = license.get(0).getLicensePeriod().getStartDate();
		XMLGregorianCalendar endDate = license.get(0).getLicensePeriod().getEndDate();
		
		license.get(0).getLicensePeriod().setStartDate(endDate);
		license.get(0).getLicensePeriod().setEndDate(startDate);
		
		File temp = createTempXMLFile (message, "createTitleInvalidDates");
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.LICENCE_DATES_NOT_IN_ORDER ==validateFile)
		{
			resultLogger.info("FXT 4.1.0.4/5 - Invalid license dates --Passed");
			resultLogger.info("FXT 4.1.1.6 - Invalid license dates --Passed");
		}
		else
		{
			resultLogger.info("FXT 4.1.0.4/5 - Invalid license dates --Failed");
			resultLogger.info("FXT 4.1.1.6 - Invalid license dates --Failed");
		}
			
		
		assertEquals(MessageValidationResult.LICENCE_DATES_NOT_IN_ORDER, validateFile);
		
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testCreateTitleUnknownChannel_FXT_4_1_0_6() throws IOException, Exception {
		
		logger.info("Starting FXT 4.1.0.6 - CreateOrUpdateTitle message has unknown channel");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		
		List<License> license = ((CreateOrUpdateTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0)).getRights().getLicense();
		
		license.get(0).getChannels().getChannel().get(0).setChannelTag(UNKNOWN_CHANNEL_TAG);
		license.get(0).getChannels().getChannel().get(0).setChannelName(UNKOWN_CHANNEL_NAME);
		
		File temp = createTempXMLFile (message, "createTitleUnknownChannel");
		
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		if (MessageValidationResult.UNKOWN_CHANNEL ==validateFile)
			resultLogger.info("FXT 4.1.0.6 - CreateOrUpdateTitle message has unknown channel --Passed");
		else
			resultLogger.info("FXT 4.1.0.6 - CreateOrUpdateTitle message has unknown channel --Failed");
		
		assertEquals(MessageValidationResult.UNKOWN_CHANNEL, validateFile);
		Util.deleteFiles(temp.getAbsolutePath());
	}
	
	
	@Test
	@Category (ValidationTests.class)
	public void testCreateTitleTitleIdNull() throws IOException, Exception {

		logger.info("TitleID is null");
		logger.info("TitleID is missing from xml");
		
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);

		CreateOrUpdateTitle coup = ((CreateOrUpdateTitle) message.getActions().
				getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0));		
		coup.setTitleID(null);
		File temp = createTempXMLFile (message, "createTitleTitleIdNull", false);

		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validator.validateFile(temp.getAbsolutePath()));
	}
	
	private PlaceholderMessage buildCreateTitle (String titleID) throws DatatypeConfigurationException {
		
		String programTitle = new MSTitleDescription().getShowAShowTitle();
		
		TitleDescriptionType tdt = new TitleDescriptionType();
		tdt = buildTitleDescription(tdt, programTitle);
		RightsType rights = new RightsType();
		rights = buildRights(rights);
		
		CreateOrUpdateTitle coup = new CreateOrUpdateTitle();
		coup.setTitleID(titleID);
		coup.setTitleDescription(tdt);
		coup.setRights(rights);
		
		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(coup);
		
		PlaceholderMessage message = new PlaceholderMessage();
		message.setMessageID("MESSAGE_ID");
		message.setSenderID("SENDER_ID");
		message.setActions(actions);
		
		return message;
	}
	
	private TitleDescriptionType buildTitleDescription (TitleDescriptionType titleDescription, String programTitle) {
		
		titleDescription.setProgrammeTitle(programTitle);
		Random randomGenerator = new Random();
		titleDescription.setProductionNumber(Integer.toString(randomGenerator.nextInt(50)));
		titleDescription.setEpisodeTitle("EpisodeTitle");
		titleDescription.setSeriesNumber(new BigInteger(Integer.toString(randomGenerator.nextInt(6))));
		titleDescription.setEpisodeNumber(new BigInteger(Integer.toString(randomGenerator.nextInt(12))));
		titleDescription.setYearOfProduction(new BigInteger("2004"));
		titleDescription.setCountryOfProduction("United Kingdom");
		titleDescription.setStyle("Series");
		titleDescription.setShow(programTitle);
		
		return titleDescription;
	}
	
	private RightsType buildRights (RightsType rights) throws DatatypeConfigurationException {
		
		License license = new License();
		
		LicenseHolderType licenseHolder = new LicenseHolderType();
		LicensePeriodType licensePeriod = new LicensePeriodType();
		ChannelType channel = new ChannelType();
		
		licenseHolder.setOrganisationID("FOXTEL");
		licenseHolder.setOrganisationName("Foxtel ManagementPty Ltd");
		license.setLicenseHolder(licenseHolder);
		
		XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN1st);
		licensePeriod.setStartDate(startDate);
		XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN10th);
		licensePeriod.setEndDate(endDate);
		license.setLicensePeriod(licensePeriod);
		
		channel.setChannelTag("FOX");
		channel.setChannelName("FOX8");
		Channels channels = new Channels();
		channels.getChannel().add(channel);
		license.setChannels(channels);
		
		rights.getLicense().add(license);
		
		return rights;
	}


}
