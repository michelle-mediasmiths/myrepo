package com.medismiths.foxtel.mpa.validation;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;


import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClient;

public class MaterialExchangeValidator extends MessageValidator<Material> {

	private final MayamClient mayamClient;

	@Inject
	public MaterialExchangeValidator(Unmarshaller unmarshaller,
			MayamClient mayamClient, ReceiptWriter receiptWriter,
			SchemaValidator schemaValidator) throws SAXException {
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient = mayamClient;
	}

	private static Logger logger = Logger
			.getLogger(MaterialExchangeValidator.class);

	@Override
	protected MessageValidationResult validateMessage(Material message) {
		logger.fatal("NO VALIDATION PERFORMED");
		return MessageValidationResult.IS_VALID;
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException {

		if (!(unmarshalled instanceof Material)) {
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a PlaceholderMessage",
					unmarshalled.getClass().toString()));
		}

	}

}
