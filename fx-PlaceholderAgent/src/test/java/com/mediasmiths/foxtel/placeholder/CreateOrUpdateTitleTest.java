package com.mediasmiths.foxtel.placeholder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
import com.mediasmiths.foxtel.messagetests.ResultLogger;
import com.mediasmiths.foxtel.placeholder.categories.ProcessingTests;
import com.mediasmiths.foxtel.placeholder.categories.ValidationTests;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class CreateOrUpdateTitleTest extends PlaceHolderMessageShortTest {

	private final static String NEW_TITLE = "NEW_TITLE";
	private static Logger logger = Logger.getLogger(CreateOrUpdateTitleTest.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

	public CreateOrUpdateTitleTest() throws JAXBException, SAXException, IOException {
		super();
	}

	@Test
	@Category(ValidationTests.class)
	public void testValidCreateTitle() throws Exception {
		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);
		File temp = createTempXMLFile(pm, "validCreateTitle");

		// test that the generated placeholder message is valid
		assertEquals(MessageValidationResult.IS_VALID,
				validator.validatePickupPackage(temp.getAbsolutePath()));
		Util.deleteFiles(temp.getAbsolutePath());
	}

	@Test
	@Category(ProcessingTests.class)
	public void testValidAddTitleProcessing() throws Exception {

		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);

		CreateOrUpdateTitle coup = (CreateOrUpdateTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);
		// prepare mock mayamClient
		when(mayamClient.titleExists(NEW_TITLE)).thenReturn(new Boolean(false));
		when(mayamClient.createTitle(coup)).thenReturn(
				MayamClientErrorCode.SUCCESS);
		// the call we are testing
		processor.processMessage(envelope);
		// verfiy update call took place
		verify(mayamClient).createTitle(coup);
	}

	@Test
	@Category(ProcessingTests.class)
	public void testValidUpdateTitleProcessing() throws Exception {
		logger.info("Starting FXT 4.1.1.5 - ID already exists");

		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(EXISTING_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);

		CreateOrUpdateTitle coup = (CreateOrUpdateTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);
		// prepare mock mayamClient
		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(
				new Boolean(true));
		when(mayamClient.updateTitle(coup)).thenReturn(
				MayamClientErrorCode.SUCCESS);
		// the call we are testing
		try{
		processor.processMessage(envelope);
		}
		catch (MessageProcessingFailedException e)
		{
			resultLogger.info("FXT 4.1.1.5 - ID already exists  --Passed");
			throw e;
		}
		
		// verfiy update call took place
		verify(mayamClient).updateTitle(coup);

	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddTitleProcessingFailsOnQueryingExistingTitle()
			throws Exception {

		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);

		// prepare mock mayamClient
		when(mayamClient.titleExists(NEW_TITLE)).thenThrow(
				new MayamClientException(MayamClientErrorCode.FAILURE));
		// the call we are testing
		processor.processMessage(envelope);
	}

	@Test(expected = MessageProcessingFailedException.class)
	@Category(ProcessingTests.class)
	public void testValidAddTitleProcessingFailesOnCreateTitle()
			throws Exception {
		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);
		MessageEnvelope<PlaceholderMessage> envelope = new MessageEnvelope<PlaceholderMessage>(new File("/dev/null"), pm);

		CreateOrUpdateTitle coup = (CreateOrUpdateTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0);
		// prepare mock mayamClient
		when(mayamClient.titleExists(NEW_TITLE)).thenReturn(new Boolean(false));
		when(mayamClient.createTitle(coup)).thenReturn(
				MayamClientErrorCode.TITLE_CREATION_FAILED);
		// the call we are testing
		processor.processMessage(envelope);
	}

	@Test
	@Category(ValidationTests.class)
	public void testCreateTitleInvalidDates() throws IOException, Exception {
		PlaceholderMessage pm = buildCreateTitleRequestSingleLicence(NEW_TITLE);

		List<License> license = ((CreateOrUpdateTitle) pm.getActions()
				.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
				.get(0)).getRights().getLicense();

		XMLGregorianCalendar startDate = license.get(0).getLicensePeriod()
				.getStartDate();
		XMLGregorianCalendar endDate = license.get(0).getLicensePeriod()
				.getEndDate();

		// swap start and end dates to create an invalid request
		license.get(0).getLicensePeriod().setStartDate(endDate);
		license.get(0).getLicensePeriod().setEndDate(startDate);

		File temp = createTempXMLFile(pm, "createTitleInvalidDates");

		//dont reject based on licence dates
		assertEquals(MessageValidationResult.IS_VALID,
				validator.validatePickupPackage(temp.getAbsolutePath()));
		Util.deleteFiles(temp.getAbsolutePath());
	}

	private PlaceholderMessage buildCreateTitleRequestSingleLicence(
			String titleID) throws DatatypeConfigurationException {
		// build request
		XMLGregorianCalendar startDate = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(JAN1st);
		XMLGregorianCalendar endDate = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(JAN10th);

		TitleDescriptionType tdt = new TitleDescriptionType();
		tdt.setProgrammeTitle("PROGRAMMETITLE");

		LicensePeriodType licencePeriod = new LicensePeriodType();
		licencePeriod.setStartDate(startDate);
		licencePeriod.setEndDate(endDate);

		LicenseHolderType licenceHolder = new LicenseHolderType();
		licenceHolder.setOrganisationID("ORGID");
		licenceHolder.setOrganisationName("ORGNAME");

		ChannelType channelType = new ChannelType();
		channelType.setChannelTag("FOX");
		channelType.setChannelName("FOX8");

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
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(
				coup);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;
	}
}
