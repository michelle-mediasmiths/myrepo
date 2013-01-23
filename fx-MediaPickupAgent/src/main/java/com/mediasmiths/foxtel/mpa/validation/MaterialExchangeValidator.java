package com.mediasmiths.foxtel.mpa.validation;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.mpa.Util;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

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
	protected MessageValidationResult validateMessage(String messagePath, Material message) {
		logger.trace("validateMessage enter");

		return validateTitle(message.getTitle(), message.getDetails());
	}

	private MessageValidationResult validateTitle(Title title, Details details) {

		final String titleID = title.getTitleID();
		// reject empty titleIDs
		if (StringUtils.isEmpty(titleID))
		{
			return MessageValidationResult.TITLEID_IS_NULL_OR_EMPTY;
		}
				
		if (Util.isProgramme(title)) {

			// title only has to exist already if we are receiving programme
			// material (not marketing)
			try {
				if (!mayamClient.titleExists(title.getTitleID())) {
					return MessageValidationResult.TITLE_DOES_NOT_EXIST;
				}
			} catch (MayamClientException e) {
				logger.error("Mayam client error", e);
				return MessageValidationResult.MAYAM_CLIENT_ERROR;
			}

			return validateProgrammeMaterial(title.getProgrammeMaterial(),details);
		} else {
			// TODO more validation of marketing material?
			return MessageValidationResult.IS_VALID;
		}

	}

	private MessageValidationResult validateProgrammeMaterial(
			ProgrammeMaterialType programmeMaterial, Details details) {

		String materialID = programmeMaterial.getMaterialID();

		//reject empty materialIDs		
		if(StringUtils.isEmpty(materialID)){
			return MessageValidationResult.MATERIALID_IS_NULL_OR_EMPTY;
		}
		
		try {
			if (!mayamClient.materialExists(materialID)) {
				return MessageValidationResult.MATERIAL_DOES_NOT_EXIST;
			}
			
			if(mayamClient.materialHasPassedPreview(materialID)){
				return MessageValidationResult.MATERIAL_HAS_ALREADY_PASSED_PREVIEW;
			}
			
			int deliveryVersion = details.getDeliveryVersion().intValue();
			int itemDeliveryVersion = mayamClient.getLastDeliveryVersionForMaterial(materialID);
			
			if (itemDeliveryVersion == -1)
			{
				//item has never been updated by a material exchange message, check if it is a placeholder
				if (!mayamClient.isMaterialPlaceholder(materialID))
				{
					return MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER;
				}
			}
			else
			{
				if (itemDeliveryVersion != (deliveryVersion - 1))
				{
					return MessageValidationResult.UNEXPECTED_DELIVERY_VERSION;
				}
			}

		} catch (MayamClientException e) {
			logger.error("Mayam client error", e);
			return MessageValidationResult.MAYAM_CLIENT_ERROR;
		}

		//not validating the passed in package information now, CR018
		return MessageValidationResult.IS_VALID;
	
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException { // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof Material)) {
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a Material", unmarshalled
							.getClass().toString()));
		}
	}

}
