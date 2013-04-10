package com.mediasmiths.foxtel.placeholder;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.SAXException;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.Source;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilePickUpFromDirectories;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.queue.SingleFilePickUp;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;
import com.mediasmiths.foxtel.placeholder.util.Util;
import com.mediasmiths.foxtel.placeholder.validation.PlaceholderMessageValidator;
import com.mediasmiths.foxtel.placeholder.validmessagepickup.FileWriter;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.validation.MayamValidator;

public abstract class PlaceHolderMessageShortTest
{

	protected PlaceholderMessageValidator validator;
	protected PlaceholderMessageProcessor processor;
	protected final MayamClient mayamClient = mock(MayamClient.class);
	protected final MayamValidator mayamValidator = mock(MayamValidator.class);
	protected final ChannelProperties ChannelProperties = new ChannelProperties()
	{

		@Override
		public boolean isValidNameForTag(String channelTag, String channelName)
		{
			return true;
		}

		@Override
		public boolean isValidFormatForTag(String channelTag, String channelFormat)
		{
			return true;
		}

		@Override
		public boolean isTagValid(String channelTag)
		{
			return true;
		}

		@Override
		public String channelGroupForChannel(String channelTag)
		{
			return "group";
		}

		@Override
		public String exportPathForChannelGroup(String channelGroupName)
		{
			return "path";
		}

		@Override
		public Set<String> groupsForChannels(List<String> channelTags)
		{
			return Collections.<String>emptySet();
		}
	};
	protected final AlertInterface alert = mock(AlertInterface.class);
	protected final String alertRecipient = "alert@foxtel.com.au";

	protected static String createMessageID() {
		return RandomStringUtils.randomAlphabetic(10);
	}

	protected static String createSenderID() {
		return RandomStringUtils.randomAlphabetic(10);
	}

	protected final static String EXISTING_TITLE = "EXISTING_TITLE";
	protected final static String PROTECTED_TITLE = "PROTECTED_TITLE";
	protected final static String NOT_EXISTING_TITLE = "NEW_TITLE";
	protected final static String ERROR_TITLE_ID = "ERROR_TITLE";
	protected final static String NEW_MATERIAL_ID = "NEW_MATERIAL";
	protected final static String EXISTING_MATERIAL_ID = "EXISTING_MATERIAL";
	protected final static String NOT_EXISTING_MATERIAL = "NEW_MATERIAL";
	protected final static String EXISTING_PACKAGE_ID = "EXISTING_PACKAGE";
	protected final static String NOT_EXISTING_PACKAGE = "NEW_PACKAGE";
	protected final static String PROTECTED_PACKAGE = "PROTECTED_PACKAGE";
	protected final static String ERROR_PACKAGE_ID = "ERROR_PACKAGE";

	protected final static String UNKNOWN_CHANNEL_TAG = "UNKNOWN_CHANNEL_TAG";
	protected final static String UNKOWN_CHANNEL_NAME = "UNKNOWN_CHANNEL_NAME";

	protected String receiptFolderPath;

	protected final static GregorianCalendar JAN1st = new GregorianCalendar(2012, 1, 1, 0, 0, 1);
	protected final static GregorianCalendar JAN10th = new GregorianCalendar(2014, 1, 10, 0, 0, 1);

	public PlaceHolderMessageShortTest()
	{
	}

	@Before
	public void before() throws IOException, JAXBException, SAXException
	{
		receiptFolderPath = Util.prepareTempFolder("RECEIPTS");

		JAXBContext jc = JAXBContext.newInstance("au.com.foxtel.cf.mam.pms");
		Unmarshaller unmarhsaller = jc.createUnmarshaller();
		Marshaller marshaller = jc.createMarshaller();
		EventService events = mock(EventService.class);
		validator = new PlaceholderMessageValidator(unmarhsaller, mayamClient, mayamValidator, new ReceiptWriter(), new SchemaValidator("PlaceholderManagement.xsd"), ChannelProperties);
		
		processor = new PlaceholderMessageProcessor(new SingleFilePickUp(new File[] {File.createTempFile("test", "test")}, "xml"), validator, new ReceiptWriter(), unmarhsaller, marshaller, mayamClient, events);

	}

	@After
	public void after()
	{
		Util.deleteFiles(receiptFolderPath);
	}

	protected PickupPackage createTempXMLFile(PlaceholderMessage pm, String description) throws IOException, Exception
	{
		return createTempXMLFile(pm, description, true);
	}

	protected PickupPackage createTempXMLFile(PlaceholderMessage pm, String description, boolean validate) throws IOException, Exception
	{
		// marshall and write to file
		FileWriter writer = new FileWriter();
		File temp = new File("/tmp/placeHolderTestData/" + description + "__" + RandomStringUtils.randomAlphabetic(6) + ".xml");

		writer.writeObjectToFile(pm, temp.getAbsolutePath(), validate);
		PickupPackage pp = new PickupPackage("xml");
		pp.addPickUp(temp);
		return pp;
	}

	protected static PlaceholderMessage buildAddMaterialRequest(
			String titleID,
			GregorianCalendar created,
			GregorianCalendar required) throws DatatypeConfigurationException
	{
		MaterialType m = buildMaterial(NEW_MATERIAL_ID, created, required);

		AddOrUpdateMaterial aum = new AddOrUpdateMaterial();
		aum.setTitleID(titleID);
		aum.setMaterial(m);

		Actions actions = new Actions();
		actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(aum);

		PlaceholderMessage pm = new PlaceholderMessage();
		pm.setMessageID(createMessageID());
		pm.setSenderID(createSenderID());
		pm.setActions(actions);
		return pm;
	}

	protected static MaterialType buildMaterial(String materialID) throws DatatypeConfigurationException
	{
		return buildMaterial(materialID, JAN1st, JAN10th);
	}

	private static MaterialType buildMaterial(String materialID, GregorianCalendar created, GregorianCalendar required)
			throws DatatypeConfigurationException
	{
		// build request
		XMLGregorianCalendar orderCreated = DatatypeFactory.newInstance().newXMLGregorianCalendar(created);
		XMLGregorianCalendar requiredBy = DatatypeFactory.newInstance().newXMLGregorianCalendar(required);

		Order order = new Order();
		order.setOrderCreated(orderCreated);

		Aggregation aggregation = new Aggregation();
		aggregation.setOrder(order);

		Source s = new Source();
		s.setAggregation(aggregation);

		MaterialType m = new MaterialType();
		m.setMaterialID(materialID);
		m.setSource(s);
		m.setRequiredBy(requiredBy);
		return m;
	}

	protected PlaceholderMessage buildAddMaterialRequest(String titleID) throws DatatypeConfigurationException
	{
		return buildAddMaterialRequest(titleID, JAN1st, JAN10th);
	}

}
