package com.mediasmiths.foxtel.mpa.validation;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
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

		return validateTitle(message.getTitle());
	}

	private MessageValidationResult validateTitle(Title title) {

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

			return validateProgrammeMaterial(title.getProgrammeMaterial());
		} else {
			// TODO more validation of marketing material?
			return MessageValidationResult.IS_VALID;
		}

	}

	private MessageValidationResult validateProgrammeMaterial(
			ProgrammeMaterialType programmeMaterial) {

		String materialID = programmeMaterial.getMaterialID();

		try {
			if (!mayamClient.materialExists(materialID)) {
				return MessageValidationResult.MATERIAL_DOES_NOT_EXIST;
			}

			if (!mayamClient.isMaterialPlaceholder(materialID)) {
				return MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER;
			}

		} catch (MayamClientException e) {
			logger.error("Mayam client error", e);
			return MessageValidationResult.MAYAM_CLIENT_ERROR;
		}

		// TODO: sanity check start\end\duration

		//validate orignial conform
		MessageValidationResult originalConform = validateOriginalConform(programmeMaterial
				.getOriginalConform());

		if (originalConform != MessageValidationResult.IS_VALID) {
			return originalConform;
		}

		return validatePackages(programmeMaterial.getPresentation());

	}

	private MessageValidationResult validateOriginalConform(
			SegmentationType originalConform) {
		if (originalConform != null) {
			logger.trace("validating original conform");
			// TODO : what validation is required for this?
			return MessageValidationResult.IS_VALID;
		} else {
			return MessageValidationResult.IS_VALID;
		}
	}

	private MessageValidationResult validatePackages(Presentation presentation) {
		if (presentation != null && presentation.getPackage() != null) {

			logger.trace("validating packages");
			for (Package pack : presentation.getPackage()) {
				try {
					if (!mayamClient.packageExists(pack.getPresentationID())) {
						return MessageValidationResult.PACKAGE_DOES_NOT_EXIST;
					}
				} catch (MayamClientException e) {
					logger.error(
							"Mayam client error querying if package exists", e);
					return MessageValidationResult.MAYAM_CLIENT_ERROR;
				}
			}
		}

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
