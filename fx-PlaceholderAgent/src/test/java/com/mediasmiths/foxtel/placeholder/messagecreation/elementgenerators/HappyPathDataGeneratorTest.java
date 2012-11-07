package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HappyPathDataGeneratorTest
{
	private static Logger logger = Logger.getLogger(HappyPathDataGeneratorTest.class);

	@Test
	public void happyPathDataGeneratorTest()
	{

		new File("/tmp/placeHolderTestData/happyPath/").mkdirs();
		
		String randomValue = RandomStringUtils.randomAlphabetic(6);
		// Creaitng QA present Test
		// Identify ID's and fileName
		String messageID = randomValue;
		String senderID = randomValue;
		String fileName = "QAPresent";
		boolean qualityAssurance = true;
		CreateTitleXml(messageID, senderID, fileName);
		CreateMaterialXml(messageID, senderID, fileName, qualityAssurance);
		CreatePackageXml(senderID, fileName);

		// Identify ID's and fileName
		randomValue = RandomStringUtils.randomAlphabetic(6);
		messageID = randomValue;
		senderID = randomValue;
		fileName = "noQA";
		qualityAssurance = false;
	
		CreateTitleXml(messageID, senderID, fileName);
		CreateMaterialXml(messageID, senderID, fileName, qualityAssurance);
		CreatePackageXml(senderID, fileName);

	}

	private static void CreateTitleXml(String messageID, String senderID, String fileName)
	{
		// CreateOrUpdateTitle
		String titleID = "testTitle";
		String purgeProtect="true";
		String restrictAccess="true";
		
		//TitleDescription
		String programmeTitle = "testProgrammeTitle";
		String productionNumber="132213";
		String episodeTitle="testerEpisode";
		String seriesNumber="2";
		String episodeNumber="12";
		String yearOfProduction="1992";
		String countryOfProduction="Ireland";
		String style="Country";
		String show="Magicians";
		
		// LicenseData
		String organisationID = "ORGID";
		String organisationName = "ORGNAME";
		String startDate = "2000-02-01T00:00:01.000Z";
		String endDate = "2000-02-01T00:00:01.000Z";
		String channelTag = "108";
		String channelName = "Fox8";
		
		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("PlaceholderMessage");
			doc.appendChild(rootElement);

			// Setting elements in PlaceholderMessage
			Attr placeHolderAttr = doc.createAttribute("senderID");
			placeHolderAttr.setValue(senderID);
			rootElement.setAttributeNode(placeHolderAttr);
			Attr placeHolderMsgAttr = doc.createAttribute("messageID");
			placeHolderMsgAttr.setValue(messageID);
			rootElement.setAttributeNode(placeHolderMsgAttr);
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://foxtel.com.au/schemas/MAM/Placeholders/001/000");
			rootElement.setAttributeNode(xmlns);

			Element actions = doc.createElement("Actions");
			rootElement.appendChild(actions);

			//CreateOrUpdateTitle
			Element createOrUpdateTitle = doc.createElement("CreateOrUpdateTitle");
			createOrUpdateTitle.setAttribute("titleID", titleID);
			createOrUpdateTitle.setAttribute("purgeProtect", purgeProtect);
			createOrUpdateTitle.setAttribute("restrictAccess", restrictAccess);
			actions.appendChild(createOrUpdateTitle);
			

			// Title Description Section
			Element TitleDescription = doc.createElement("TitleDescription");
			createOrUpdateTitle.appendChild(TitleDescription);
			Element ProgrammeTitle = doc.createElement("ProgrammeTitle");
			ProgrammeTitle.appendChild(doc.createTextNode(programmeTitle));
			TitleDescription.appendChild(ProgrammeTitle);
			Element ProductionNumber = doc.createElement("ProductionNumber");
			ProductionNumber.appendChild(doc.createTextNode(productionNumber));
			TitleDescription.appendChild(ProductionNumber);
			Element EpisodeTitle = doc.createElement("EpisodeTitle");
			EpisodeTitle.appendChild(doc.createTextNode(episodeTitle));
			TitleDescription.appendChild(EpisodeTitle);
			Element SeriesNumber = doc.createElement("SeriesNumber");
			SeriesNumber.appendChild(doc.createTextNode(seriesNumber));
			TitleDescription.appendChild(SeriesNumber);
			Element EpisodeNumber = doc.createElement("EpisodeNumber");
			EpisodeNumber.appendChild(doc.createTextNode(episodeNumber));
			TitleDescription.appendChild(EpisodeNumber);
			Element YearOfProduction = doc.createElement("YearOfProduction");
			YearOfProduction.appendChild(doc.createTextNode(yearOfProduction));
			TitleDescription.appendChild(YearOfProduction);
			Element CountryOfProduction = doc.createElement("CountryOfProduction");
			CountryOfProduction.appendChild(doc.createTextNode(countryOfProduction));
			TitleDescription.appendChild(CountryOfProduction);
			Element Style = doc.createElement("Style");
			Style.appendChild(doc.createTextNode(style));
			TitleDescription.appendChild(Style);
			Element Show = doc.createElement("Show");
			Show.appendChild(doc.createTextNode(show));
			TitleDescription.appendChild(Show);
			
			//Beginning rights section
			Element Rights = doc.createElement("Rights");
			createOrUpdateTitle.appendChild(Rights);
			Element License = doc.createElement("License");
			Rights.appendChild(License);

			// License Section
			Element LicenseHolder = doc.createElement("LicenseHolder");
			LicenseHolder.setAttribute("organisationID", organisationID);
			LicenseHolder.setAttribute("organisationName", organisationName);
			License.appendChild(LicenseHolder);
			Element LicensePeriod = doc.createElement("LicensePeriod");
			LicensePeriod.setAttribute("startDate", startDate);
			LicensePeriod.setAttribute("endDate", endDate);
			License.appendChild(LicensePeriod);
			Element Channels = doc.createElement("Channels");
			License.appendChild(Channels);
			Element Channel = doc.createElement("Channel");
			Channel.setAttribute("channelName", channelName);
			Channel.setAttribute("channelTag", channelTag);
			Channels.appendChild(Channel);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/tmp/placeHolderTestData/happyPath/CreateTitle" + fileName + ".xml"));

			transformer.transform(source, result);

			logger.info("Create Title file saved: CreateTitle" + fileName + ".xml");

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (TransformerException tfe)
		{
			tfe.printStackTrace();
		}
	}
	private static void CreateMaterialXml(String messageID, String senderID, String fileName, boolean QA)
	{
		// Valid CreateTitle

		String titleID = "testTitle";
		String materialID = "testNew_Material";
		String requiredBy = "2000-02-10T00:00:01.000Z";
		String requiredFormat = "SD";
		String qualityCheckTask;
		if (QA = true)
		{
			qualityCheckTask = "AutomaticOnIngest";
		}
		else
			qualityCheckTask = "No";

		// SourceData
		String orderCreated = "2000-02-01T00:00:01.000Z";
		String orderReference = "abc123";
		String aggregatorID = "def456";
		String aggregatorName = "aggregator";

		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("PlaceholderMessage");
			doc.appendChild(rootElement);

			// Setting elements in PlaceholderMessage
			Attr placeHolderAttr = doc.createAttribute("senderID");
			placeHolderAttr.setValue(senderID);
			rootElement.setAttributeNode(placeHolderAttr);
			Attr placeHolderMsgAttr = doc.createAttribute("messageID");
			placeHolderMsgAttr.setValue(messageID);
			rootElement.setAttributeNode(placeHolderMsgAttr);
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://foxtel.com.au/schemas/MAM/Placeholders/001/000");
			rootElement.setAttributeNode(xmlns);

			Element actions = doc.createElement("Actions");
			rootElement.appendChild(actions);

			Element AddOrUpdateMaterial = doc.createElement("AddOrUpdateMaterial");
			AddOrUpdateMaterial.setAttribute("titleID", titleID);
			actions.appendChild(AddOrUpdateMaterial);

			Element Material = doc.createElement("Material");
			Material.setAttribute("materialID", materialID);
			AddOrUpdateMaterial.appendChild(Material);

			Element RequiredBy = doc.createElement("RequiredBy");
			RequiredBy.appendChild(doc.createTextNode(requiredBy));
			Material.appendChild(RequiredBy);

			Element RequiredFormat = doc.createElement("RequiredFormat");
			RequiredFormat.appendChild(doc.createTextNode(requiredFormat));
			Material.appendChild(RequiredFormat);

			Element QualityCheckTask = doc.createElement("QualityCheckTask");
			QualityCheckTask.appendChild(doc.createTextNode(qualityCheckTask));
			Material.appendChild(QualityCheckTask);

			Element Source = doc.createElement("Source");
			Material.appendChild(Source);
			Element Aggregation = doc.createElement("Aggregation");
			Source.appendChild(Aggregation);
			Element Order = doc.createElement("Order");
			Aggregation.appendChild(Order);

			Element OrderCreated = doc.createElement("OrderCreated");
			OrderCreated.appendChild(doc.createTextNode(orderCreated));
			Order.appendChild(OrderCreated);
			Element OrderReference = doc.createElement("OrderReference");
			OrderReference.appendChild(doc.createTextNode(orderReference));
			Order.appendChild(OrderReference);

			Element Aggregator = doc.createElement("Aggregator");
			Aggregator.setAttribute("aggregatorID", aggregatorID);
			Aggregator.setAttribute("aggregatorName", aggregatorName);
			Aggregation.appendChild(Aggregator);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/tmp/placeHolderTestData/HappyPath/CreateMaterial" + fileName
					+ ".xml"));

			transformer.transform(source, result);

			logger.info("Create Material file saved: CreateMaterial" + fileName + ".xml");

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (TransformerException tfe)
		{
			tfe.printStackTrace();
		}
	}

	private static void CreatePackageXml(String senderID, String fileName)
	{
		// Valid CreateTitle

		String titleID = "testTitle";
		String presentationID = "testNew_Package";
		String materialID = "testNew_Material";
		String presentationFormat = "HD";
		String classification = "G";
		String consumerAdvice = "L";
		String numberOfSegments = "1";
		String targetDate="2011-05-15T00:00:00+10:00";
		String notes="Testing Notes";

		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("PlaceholderMessage");
			doc.appendChild(rootElement);

			// Setting elements in PlaceholderMessage
			Attr placeHolderAttr = doc.createAttribute("senderID");
			placeHolderAttr.setValue(senderID);
			rootElement.setAttributeNode(placeHolderAttr);
			Attr placeHolderMsgAttr = doc.createAttribute("messageID");
			placeHolderMsgAttr.setValue(RandomStringUtils.randomAlphabetic(6));
			rootElement.setAttributeNode(placeHolderMsgAttr);
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://foxtel.com.au/schemas/MAM/Placeholders/001/000");
			rootElement.setAttributeNode(xmlns);

			Element actions = doc.createElement("Actions");
			rootElement.appendChild(actions);

			Element AddOrUpdatePackage = doc.createElement("AddOrUpdatePackage");
			AddOrUpdatePackage.setAttribute("titleID", titleID);
			actions.appendChild(AddOrUpdatePackage);

			Element Package = doc.createElement("Package");
			Package.setAttribute("presentationID", presentationID);
			AddOrUpdatePackage.appendChild(Package);

			Element MaterialID = doc.createElement("MaterialID");
			MaterialID.appendChild(doc.createTextNode(materialID));
			Package.appendChild(MaterialID);

			Element PresentationFormat = doc.createElement("PresentationFormat");
			PresentationFormat.appendChild(doc.createTextNode(presentationFormat));
			Package.appendChild(PresentationFormat);

			Element Classification = doc.createElement("Classification");
			Classification.appendChild(doc.createTextNode(classification));
			Package.appendChild(Classification);

			Element ConsumerAdvice = doc.createElement("ConsumerAdvice");
			ConsumerAdvice.appendChild(doc.createTextNode(consumerAdvice));
			Package.appendChild(ConsumerAdvice);

			Element NumberOfSegments = doc.createElement("NumberOfSegments");
			NumberOfSegments.appendChild(doc.createTextNode(numberOfSegments));
			Package.appendChild(NumberOfSegments);
			
			Element targetdate = doc.createElement("Targetdate");
			targetdate.appendChild(doc.createTextNode(targetDate));
			Package.appendChild(targetdate);
			
			Element Notes = doc.createElement("notes");
			Notes.appendChild(doc.createTextNode(notes));
			Package.appendChild(Notes);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/tmp/placeHolderTestData/HappyPath/CreatePackage" + fileName
					+ ".xml"));

			transformer.transform(source, result);

			logger.info("Create package file saved: CreatePackage" + fileName + ".xml");

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (TransformerException tfe)
		{
			tfe.printStackTrace();
		}
	}

}
