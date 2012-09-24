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
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.IOUtils;
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
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.HelperMethods;
import com.mediasmiths.foxtel.placeholder.PlaceHolderMessageShortTest;

import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

import com.mediasmiths.foxtel.placeholder.junit.PlaceholderMessageShortTest;

public class CreateOrUpdateTitleTest extends PlaceHolderMessageShortTest {
	
	private final static String NEW_TITLE = "NEW_TITLE";
	
	public CreateOrUpdateTitleTest() throws JAXBException, SAXException, IOException {
		super();
	}
	
	@Test
	@Category(ProcessingTests.class)
	public void testValidCreateTitleProcessing() throws Exception {
		
		System.out.println("Processing validation");
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
	public void testCreateTitleXSDInvalid() throws Exception {
		
		System.out.println("FXT 4.1.1.2 - Non XSD compliance");
		File temp = File.createTempFile("NonXSDConformingFile", ".xml");
		IOUtils.write("InvalidCreateTitle", new FileOutputStream(temp));
		MessageValidationResult validateFile = validator.validateFile(temp.getAbsolutePath());
		
		assertEquals(MessageValidationResult.FAILS_XSD_CHECK, validateFile);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testValidCreateTitle() throws Exception {
		
		System.out.println("FXT 4.1.1.3/4 - XSD Compliance/ Non-existing ID");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		File temp = createTempXMLFile(message, "validCreateTitle");
		
		assertEquals(MessageValidationResult.IS_VALID, 
				validator.validateFile(temp.getAbsolutePath()));
	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidCreateTitleProcessingFailsOnExistingTitle() throws Exception {
		
		System.out.println("FXT 4.1.1.5 - ID already exists");
		PlaceholderMessage message = buildCreateTitle(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), message);
		
		when(mayamClient.titleExists(EXISTING_TITLE)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));
		processor.processMessage(envelope);
	}
	
	@Test
	@Category(ValidationTests.class)
	public void testCreateTitleInvalidDates() throws IOException, Exception {
		
		System.out.println("FXT 4.1.0.4/5 - Invalid license dates");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		
		List<License> license = ((CreateOrUpdateTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0)).getRights().getLicense();
		
		XMLGregorianCalendar startDate = license.get(0).getLicensePeriod().getStartDate();
		XMLGregorianCalendar endDate = license.get(0).getLicensePeriod().getEndDate();
		
		license.get(0).getLicensePeriod().setStartDate(endDate);
		license.get(0).getLicensePeriod().setEndDate(startDate);
		
		File temp = createTempXMLFile (message, "createTitleInvalidDates");
		
		assertEquals(MessageValidationResult.LICENCE_DATES_NOT_IN_ORDER,
				validator.validateFile(temp.getAbsolutePath()));
	}
	
	@Test
	@Category (ValidationTests.class)
	public void testCreateTitleUnknownChannel() throws IOException, Exception {
		
		System.out.println("FXT 4.1.0.6 - CreateOrUpdateTitle message has unknown channel");
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		
		List<License> license = ((CreateOrUpdateTitle) message.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0)).getRights().getLicense();
		
		license.get(0).getChannels().getChannel().get(0).setChannelTag(UNKNOWN_CHANNEL_TAG);
		license.get(0).getChannels().getChannel().get(0).setChannelName(UNKOWN_CHANNEL_NAME);
		
		File temp = createTempXMLFile (message, "createTitleUnknownChannel");
		
		assertEquals(MessageValidationResult.UNKOWN_CHANNEL,
				validator.validateFile(temp.getAbsolutePath()));
	}
	
	/*@Test
	@Category (ValidationTests.class)
	public void testCreateTitleTitleIdNull() throws IOException, Exception {
		
		System.out.println("TitleID is null");
		
		PlaceholderMessage message = buildCreateTitle(NEW_TITLE);
		
		CreateOrUpdateTitle coup = ((CreateOrUpdateTitle) message.getActions().
				getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0));		
		coup.setTitleID(null);
		
		File temp = createTempXMLFile (message, "createTitleTitleIdNull");
		
		assertEquals(MessageValidationResult.TITLEID_IS_NULL, validator.validateFile(temp.getAbsolutePath()));
	}*/
	
	private PlaceholderMessage buildCreateTitle (String titleID) throws DatatypeConfigurationException {
		
		HelperMethods helper = new HelperMethods();
		String programTitle = helper.validProgramTitle();
		
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
		
		HelperMethods method = new HelperMethods();
		//XMLGregorianCalendar xmlCal = method.giveValidDate();
		XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN1st);
		licensePeriod.setStartDate(startDate);
		//xmlCal = method.giveValidDate();
		XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(JAN10th);
		licensePeriod.setEndDate(endDate);
		license.setLicensePeriod(licensePeriod);
		
		channel.setChannelTag("FO8");
		channel.setChannelName("Fox 8");
		Channels channels = new Channels();
		channels.getChannel().add(channel);
		license.setChannels(channels);
		
		rights.getLicense().add(license);
		
		return rights;
	}


}
