package com.mediasmiths.foxtel.agent.validation;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

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
	protected boolean againstXSD(File xmlFile)
	{
		return schemaValidator.isValid(xmlFile);
	}

	/**
	 * Validates an xml file according to the rules
	 * 
	 * @param pp
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws MayamClientException
	 */
	@SuppressWarnings("unchecked")
	public MessageValidationResultPackage<T> validatePickupPackage(PickupPackage pp)
	{
		logger.debug("Validating file " + pp);
		// first check the xml file conforms to the schema
		boolean againstXSD = againstXSD(pp.getPickUp("xml"));

		if (!againstXSD)
		{
			return new MessageValidationResultPackage<T>(pp, MessageValidationResult.FAILS_XSD_CHECK);
		}

		// xml file has valid schema, unmarshall then continue validation
		T message = null;

		try
		{
			
			message = (T) unmarshallFile(pp.getPickUp("xml"));
		}
		catch (JAXBException e)
		{
			logger.fatal("Failed to unmarshall file " + pp + " that had validated against schema", e);
			return new MessageValidationResultPackage<T>(pp, MessageValidationResult.FAILED_TO_UNMARSHALL);
		}
		catch (ClassCastException cce)
		{
			logger.fatal("Unmarshalled file that conformed to schema that did not have the expected type", cce);
			return new MessageValidationResultPackage<T>(pp, MessageValidationResult.UNEXPECTED_TYPE);
		}

		MessageValidationResult validationResult = validateMessage(pp, message);
		
		return new MessageValidationResultPackage<T>(pp, message, validationResult);

	}

	protected abstract MessageValidationResult validateMessage(PickupPackage pp, T message);


	protected Object unmarshallFile(File xml) throws JAXBException {
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
