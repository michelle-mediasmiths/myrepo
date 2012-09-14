package com.mediasmiths.foxtel.agent;

import java.io.File;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

public abstract class MessageValidator<T> {

	private static Logger logger = Logger.getLogger(MessageValidator.class);

	// an unmarshaller for turning xml files into objects
	private final Unmarshaller unmarhsaller;
	// schema will be used for validating files against the schema
	private final SchemaValidator schemaValidator;
		
	private final ReceiptWriter receiptWriter;

	@Inject
	public MessageValidator(Unmarshaller unmarshaller, ReceiptWriter receiptWriter, SchemaValidator schemaValidator)
			throws SAXException {
		this.unmarhsaller = unmarshaller;
		this.schemaValidator = schemaValidator;
		this.receiptWriter=receiptWriter;
	}

	/**
	 * Checks the structure of a given xml file against the xsd
	 * 
	 * @param filepath
	 * @return pass
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	protected final boolean againstXSD(String filepath)
	{
		return schemaValidator.isValid(new File(filepath));
	}

	/**
	 * Validates an xml file according to the rules
	 * 
	 * @param filepath
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws MayamClientException
	 */
	@SuppressWarnings("unchecked")
	public final MessageValidationResult validateFile(String filepath)
	 {

		// first check the xml file conforms to the schema
		boolean againstXSD = againstXSD(filepath);

		if (!againstXSD) {
			return MessageValidationResult.FAILS_XSD_CHECK;
		}

		// xml file has valid schema, unmarshall then continue validation
		T message = null;

		try {
			message = (T) unmarshallFile(new File(filepath));
			
		} catch (JAXBException e) {
			logger.fatal("Failed to unmarshall file " + filepath
					+ " that had validated against schema");
			return MessageValidationResult.FAILED_TO_UNMARSHALL;
		} catch (ClassCastException cce) {
			logger.fatal("Unmarshalled file that conformed to schema that did not have the expected type");
			return MessageValidationResult.UNEXPECTED_TYPE;
		} 
		
		return validateMessage(message);

	}

	protected abstract MessageValidationResult validateMessage(T message);


	protected final Object unmarshallFile(File xml) throws JAXBException {
		Object unmarshalled = unmarhsaller.unmarshal(xml);
		logger.debug(String.format("unmarshalled object of type %s",
				unmarshalled.getClass().toString()));
		typeCheck(unmarshalled);
		return unmarshalled;
	}
	
	/**
	 *  Called to check the type of an unmarshalled object, left up to implementing classes as (unmarshalled instanceof T) cannot be checked;
	 * @param unmsarhalled
	 * @throws ClassCastException if the unmarshalled object is not of the expected type
	 */
	protected abstract void typeCheck(Object unmarshalled) throws ClassCastException;

	public ReceiptWriter getReceiptWriter() {
		return receiptWriter;
	}

}
