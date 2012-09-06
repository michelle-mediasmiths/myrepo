package com.mediasmiths.foxtel.placeholder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Validator {
	
	private static Logger logger = Logger.getLogger(Validator.class);

	/**
	 * Checks the structure of a given xml file against the xsd
	 * 
	 * @param filepath
	 * @return pass
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static boolean againstXSD(String filepath) throws SAXException,
			ParserConfigurationException, IOException {

		boolean pass = false;

		try {
			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(
					Validator.class.getClassLoader().getResourceAsStream("PlaceholderManagement.xsd")));
			javax.xml.validation.Validator validator = schema.newValidator();
			validator.validate(new StreamSource(filepath));
			pass = true;
		} catch (Exception e) {
			logger.error("invalid structure",e);			
		}

		return pass;
	}

	public static Document setUpDoc(File xmlFile) throws SAXException,
			IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlFile);
		return doc;
	}

	/**
	 * Reads and returns the value of a requested element
	 * 
	 * @param filepath
	 * @param tagName
	 * @param targetElement
	 * @return
	 */
	public static String readXMLElement(String filepath, String targetElement) {

		String returnedElement = null;
		try {

			File xmlFile = new File(filepath);
			Document doc = setUpDoc(xmlFile);

			NodeList nList = doc.getElementsByTagName("Actions");

			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					returnedElement = getTagValue(targetElement, element);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnedElement;
	}

	/**
	 * Gets the value of an element
	 * 
	 * @param tag
	 * @param element
	 * @return
	 */
	public static String getTagValue(String tag, Element element) {
		NodeList nList = element.getElementsByTagName(tag).item(0)
				.getChildNodes();
		Node value = (Node) nList.item(0);
		return value.getNodeValue().toString();
	}

	/**
	 * Reads and returns the value of an attribute
	 * 
	 * @param filepath
	 * @param tagName
	 * @param targetAttribute
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static String readXMLAttribute(String filepath, String tagName,
			String targetAttribute) throws SAXException, IOException,
			ParserConfigurationException {
		String returnedAttribute = null;

		File xmlFile = new File(filepath);
		Document doc = setUpDoc(xmlFile);

		NodeList nodeList = doc.getElementsByTagName(tagName);

		for (int i = 0; i < nodeList.getLength(); i++) {
			returnedAttribute = nodeList.item(i).getAttributes()
					.getNamedItem(targetAttribute).toString();

			returnedAttribute = returnedAttribute.replace('"', '\0');
			int startIndex = returnedAttribute.indexOf('=');
			returnedAttribute = returnedAttribute.substring(startIndex + 1);
			// System.out.println(targetAttribute + ": " + date);
		}
		return returnedAttribute;
	}

	/**
	 * Returns true if a given element exists in a given file
	 * 
	 * @param filepath
	 * @param targetElement
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static boolean searchForElement(String filepath, String targetElement)
			throws ParserConfigurationException, SAXException, IOException {

		boolean contains = false;

		try {
			File xmlFile = new File(filepath);
			if (xmlFile.exists()) {
				Document doc = setUpDoc(xmlFile);
				NodeList list = doc.getElementsByTagName("*");

				for (int i = 0; i < list.getLength(); i++) {
					Element element = (Element) list.item(i);
					if (element.getNodeName().toString().equals(targetElement)) {
						contains = true;
					}
				}
			} else {
				System.out.println("ERROR: File not found");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contains;
	}

	/**
	 * Checks if two dates are in the right order
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean validateDates(Date date1, Date date2) {

		boolean valid = false;
		int result = date1.compareTo(date2);
		if (result > 0) {
			valid = false;
		} else if (result == 0) {
			valid = false;
		} else {
			valid = true;
		}

		if (!valid) {
			System.out.println("ERROR: invalid dates");
		}
		return valid;
	}

	/**
	 * Converts a String into a Date
	 * 
	 * @param date
	 * @return
	 */
	public static Date stringToDate(String date) {

		Date dDate = null;

		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			dDate = (Date) formatter.parse(date.replaceAll("\\p{Cntrl}", ""));
			// System.out.println("Date is: " + dDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dDate;
	}

	/**
	 * Validates an xml file according to the rules
	 * 
	 * @param filepath
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static boolean validateFile(String filepath) throws SAXException,
			ParserConfigurationException, IOException {

		boolean validSchema = false;

		boolean againstXSD = againstXSD(filepath);

		if (againstXSD) {
			String[] schemaTitles = { "CreateOrUpdateTitle", "AddOrUpdateMaterial",
					"AddOrUpdatePackage", "DeletePackage", "DeleteMaterial",
					"PurgeTitle" };
			String schema = null;
			for (int i = 0; i < schemaTitles.length; i++) {
				boolean contains = searchForElement(filepath, schemaTitles[i]);
				if (contains) {
					schema = schemaTitles[i];
					contains = false;
				}
			}

			if (schema.equals("CreateOrUpdateTitle")) {
				validSchema = validateCreateTitle(filepath);
			} else if (schema.equals("AddOrUpdateMaterial")) {
				validSchema = validateCreateItem(filepath);
			} else if (schema.equals("AddOrUpdatePackage")) {
				validSchema = validateCreateTxPackage(filepath);
			} else if (schema.equals("DeletePackage")) {
				validSchema = validateDeletePackage(filepath);
			} else if (schema.equals("DeleteMaterial")) {
				validSchema = validateDeleteMaterial(filepath);
			} else if (schema.equals("PurgeTitle")) {
				validSchema = validateDeleteTitle(filepath);
			}

			if (validSchema) {
				System.out.println("Schema is valid");
			} else {
				System.out.println("Invalid schema");
			}
			
			return validSchema;
		}
		return againstXSD;
	}

	/**
	 * Checks a CreateOrUpdateTitle schema is valid startDate is before endDate
	 * 
	 * @param filepath
	 * @return
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static boolean validateCreateTitle(String filepath)
			throws SAXException, ParserConfigurationException, IOException {

		System.out.println("Validating title...");
		boolean pass = false;

		String sStartDate = readXMLAttribute(filepath, "LicensePeriod",
				"startDate");
		String sEndDate = readXMLAttribute(filepath, "LicensePeriod", "endDate");
		Date dStartDate = stringToDate(sStartDate);
		Date dEndDate = stringToDate(sEndDate);
		boolean dates = validateDates(dStartDate, dEndDate);

		if (dates) {
			pass = true;
		}

		System.out.println("Title schema valid: " + pass);
		return pass;
	}

	/**
	 * Checks that an AddOrUpdateMaterial schema is valid existing title, existing title is
	 * valid, titleId matches, OrderCreated is before RequiredBy
	 * 
	 * @param filepath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static boolean validateCreateItem(String filepath)
			throws ParserConfigurationException, SAXException, IOException {

		System.out.println("Validating item...");
		boolean pass = false;

		boolean existingTitle = searchForElement(filepath,
				"CreateOrUpdateTitle");

		if (existingTitle) {
			boolean createTitleValid = validateCreateTitle(filepath);
			if (createTitleValid) {
				String titleTitleID = readXMLAttribute(filepath,
						"CreateOrUpdateTitle", "titleID");
				String itemTitleID = readXMLAttribute(filepath, "AddOrUpdateMaterial",
						"titleID");

				if (titleTitleID.equals(itemTitleID)) {
					String sCreated = readXMLElement(filepath, "OrderCreated");
					String sRequired = readXMLElement(filepath, "RequiredBy");
					Date dCreated = stringToDate(sCreated);
					Date dRequired = stringToDate(sRequired);
					boolean dates = validateDates(dCreated, dRequired);

					if (dates) {
						pass = true;
					}
				} else {
					System.out.println("ERROR: TitleID's dont match");
				}
			}
		} else {
			System.out.println("ERROR: No existing Title");
		}
		System.out.println("Item schema valid: " + pass);
		return pass;
	}

	/**
	 * Checks if an AddOrUpdatePackage schema is vaild existing item, existing
	 * item is valid, titleID matches, TargetDate is before RequiredBy
	 * 
	 * @param filepath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static boolean validateCreateTxPackage(String filepath)
			throws ParserConfigurationException, SAXException, IOException {

		System.out.println("Validating txPackage...");
		boolean pass = false;

		boolean existingItem = searchForElement(filepath, "AddOrUpdateMaterial");

		if (existingItem) {
			boolean createItemValid = validateCreateItem(filepath);

			if (createItemValid) {
				String itemTitleID = readXMLAttribute(filepath, "AddOrUpdateMaterial",
						"titleID");
				String txTitleID = readXMLAttribute(filepath,
						"AddOrUpdatePackage", "titleID");

				if (itemTitleID.equals(txTitleID)) {
					String sTarget = readXMLElement(filepath, "TargetDate");
					String sRequired = readXMLElement(filepath, "RequiredBy");
					Date dTarget = stringToDate(sTarget);
					Date dRequired = stringToDate(sRequired);
					boolean dates = validateDates(dTarget, dRequired);

					if (dates) {
						pass = true;
					}
				} else {
					System.out.println("ERROR: Title ID's don't match");
				}
			} 
		} else {
			System.out.println ("ERROR: No existing Title");
		}
		System.out.println("Tx schema valid: " + pass);
		return pass;
	}

	/**
	 * Checks if a DeletePackage schema is valid
	 * 
	 * @param filepath
	 * @return
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static boolean validateDeletePackage(String filepath)
			throws SAXException, ParserConfigurationException, IOException {

		boolean pass = false;

		boolean againstXSD = againstXSD(filepath);
		if (againstXSD) {
			pass = true;
		}

		return pass;
	}

	/**
	 * Checks if a DeleteMaterial schema is valid associated txPackage has been
	 * deleted, titleID matches
	 * 
	 * @param filepath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static boolean validateDeleteMaterial(String filepath)
			throws ParserConfigurationException, SAXException, IOException {

		boolean pass = false;

		boolean existingTx = searchForElement(filepath, "DeletePackage");
		if (existingTx) {
			boolean deleteTxValid = validateDeletePackage(filepath);

			if (deleteTxValid) {
				String txTitleID = readXMLAttribute(filepath,
						"DeletePackage", "titleID");
				String itemTitleID = readXMLAttribute(filepath, "DeleteMaterial",
						"titleID");

				if (txTitleID.equals(itemTitleID)) {
					pass = true;
				} else {
					System.out.println("ERROR: TitleID's dont't match");
				}
			}
		} else {
			System.out.println("ERROR: Existing TxPackages");
		}
		return pass;
	}

	/**
	 * Checks if a PurgeTitle schema is valid associated items have been
	 * deleted, titleID matches
	 * 
	 * @param filepath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static boolean validateDeleteTitle(String filepath)
			throws ParserConfigurationException, SAXException, IOException {

		boolean pass = false;

		boolean existingItem = searchForElement(filepath, "DeleteMaterial");
		if (existingItem) {
			boolean DeleteMaterialValid = validateDeleteMaterial(filepath);

			if (DeleteMaterialValid) {
				String itemTitleID = readXMLAttribute(filepath, "DeleteMaterial",
						"titleID");
				String titleTitleID = readXMLAttribute(filepath, "PurgeTitle",
						"titleID");

				if (itemTitleID.equals(titleTitleID)) {
					pass = true;
				} else {
					System.out.println ("ERROR: TitleID's dont match");
				}
			}
		} else {
			System.out.println ("ERROR: Existing Item");
		}
		return pass;
	}

	public static void main(String[] args) throws SAXException, IOException,
			ParserConfigurationException {

		String filepath = "/Users/alisonboal/Documents/Foxtel/XMLTests/test30_createTitle,createItem,createTxPackageInvalidDatesFAIL.xml";
		validateFile(filepath);

	}

}
