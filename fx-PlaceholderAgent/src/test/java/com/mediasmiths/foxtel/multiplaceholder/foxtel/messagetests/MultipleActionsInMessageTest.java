package com.mediasmiths.foxtel.multiplaceholder.foxtel.messagetests;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.PlaceHolderMessageShortTest;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.messagecreation.elementgenerators.MSTitleDescription;
import com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util.Util;
import com.mediasmiths.mayam.MayamClientErrorCode;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.when;

public class MultipleActionsInMessageTest extends PlaceHolderMessageShortTest
{

	private static Logger logger = Logger.getLogger(MultipleActionsInMessageTest.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);
	
	public MultipleActionsInMessageTest() throws JAXBException, SAXException, IOException {
		super();	
	}

	@Test
	public void testMultipleActionsInMessage() throws Exception{

		logger.info("Starting FXT 4.1.0.2 – Multiple Actions in Message");

		PlaceholderMessage pm = buildAddMaterialRequest(EXISTING_TITLE);
		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();

		PurgeTitle purge = new PurgeTitle();
		purge.setTitleID(EXISTING_TITLE);

		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(purge);

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.updateMaterial(((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0)).getMaterial())).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.createMaterial(((AddOrUpdateMaterial)pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0)).getMaterial(),EXISTING_TITLE)).thenReturn(MayamClientErrorCode.SUCCESS);
        when(mayamClient.purgeTitle(purge)).thenReturn(MayamClientErrorCode.SUCCESS);

		PickupPackage pp = createTempXMLFile (pm,"multipleActions");

		//test that the correct validation result is returned
		try
		{
		    processor.processPickupPackage(pp, pm);

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");

		}
		catch (MessageProcessingFailedException e)
		{

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message--Failed");

		}

		Util.deleteFiles(pp);
	}


	@Test
	public void testIBMSActionsInMessage() throws Exception{

		logger.info("Starting FXT 4.1.0.2 – Multiple Actions in Message");


		PlaceholderMessage pm = buildCreateTitle(EXISTING_TITLE);

		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();

		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(buildAddMaterialRequest(EXISTING_TITLE).getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.updateTitle((CreateOrUpdateTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0))).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.createTitle((CreateOrUpdateTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0))).thenReturn(MayamClientErrorCode.SUCCESS);

		when(mayamClient.updateMaterial(((AddOrUpdateMaterial) pm.getActions()
		                                                         .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                                         .get(1)).getMaterial())).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.createMaterial(((AddOrUpdateMaterial) pm.getActions()
		                                                         .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                                         .get(1)).getMaterial(), EXISTING_TITLE)).thenReturn(MayamClientErrorCode.SUCCESS);

		PickupPackage pp = createTempXMLFile (pm,"multipleActions");

		//test that the correct validation result is returned
		try
		{
			processor.processPickupPackage(pp, pm);

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");

		}
		catch (MessageProcessingFailedException e)
		{

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message--Failed");

		}

		Util.deleteFiles(pp);
	}


	@Test
	public void testIBMSActionsRollBackInMessage() throws Exception{

		logger.info("Starting FXT 4.1.0.2 – Multiple Actions in Message");


		PlaceholderMessage pm = buildCreateTitle(EXISTING_TITLE);

		List<Object> createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial = pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial();

		createOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial.add(buildAddMaterialRequest(EXISTING_TITLE).getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0));

		when(mayamClient.titleExists(EXISTING_TITLE)).thenReturn(true);
		when(mayamClient.updateTitle((CreateOrUpdateTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0))).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.createTitle((CreateOrUpdateTitle) pm.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0))).thenReturn(MayamClientErrorCode.SUCCESS);

		when(mayamClient.updateMaterial(((AddOrUpdateMaterial) pm.getActions()
		                                                         .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                                         .get(1)).getMaterial())).thenReturn(MayamClientErrorCode.FAILURE);
		when(mayamClient.createMaterial(((AddOrUpdateMaterial) pm.getActions()
		                                                         .getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
		                                                         .get(1)).getMaterial(), EXISTING_TITLE)).thenReturn(MayamClientErrorCode.FAILURE);

		PickupPackage pp = createTempXMLFile (pm,"multipleActions");

		//test that the correct validation result is returned
		try
		{
			processor.processPickupPackage(pp, pm);

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message --Passed");

		}
		catch (MessageProcessingFailedException e)
		{

			resultLogger.info("FXT 4.1.0.2 – Multiple Actions in Message--Failed");

		}

		Util.deleteFiles(pp);
	}



	private PlaceholderMessage buildCreateTitle (String titleID) throws DatatypeConfigurationException
	{

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
		titleDescription.setEpisodeTitle(new MSTitleDescription().getEpisodeTitle());
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
		licenseHolder.setOrganisationName("Foxtel");
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
