package com.mediasmiths.foxtel.mpa.validation;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material.IngestRecords;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class RuzzValidator extends MessageValidator<RuzzIngestRecord>
{

	private final static Logger log = Logger.getLogger(RuzzValidator.class);

	private final MayamClient mayamClient;
	
	@Inject
	public RuzzValidator(
			@Named("ruzz.unmarshaller") Unmarshaller unmarshaller,
			MayamClient mayamClient,
			ReceiptWriter receiptWriter,
			@Named("ruzz.schema.validator") SchemaValidator schemaValidator) throws SAXException
	{
		super(unmarshaller, receiptWriter, schemaValidator);
		this.mayamClient=mayamClient;
	}

	@Override
	protected MessageValidationResult validateMessage(String messagePath, RuzzIngestRecord message)
	{
		try
		{
			return validateMaterial(message.getMaterial());
		}
		catch (MayamClientException e)
		{
			log.error("failed to validate message due to mayam client exception");
			return MessageValidationResult.MAYAM_CLIENT_ERROR;
		}
	}

	private MessageValidationResult validateMaterial(Material material) throws MayamClientException
	{
		String materialID = material.getMaterialID();
		
		if(! mayamClient.materialExists(materialID)){
			log.error(String.format("material %s does not exist"));
			return MessageValidationResult.MATERIAL_DOES_NOT_EXIST;
		}
		
		if(! mayamClient.isMaterialPlaceholder(materialID)){
			log.error(String.format("material %s not a placeholder"));
			return MessageValidationResult.MATERIAL_IS_NOT_PLACEHOLDER;
		}
		
		if(mayamClient.materialHasPassedPreview(materialID)){
			return MessageValidationResult.MATERIAL_HAS_ALREADY_PASSED_PREVIEW;
		}
		
		log.info("validating message for material"+materialID);
		
		MessageValidationResult detailsValid = validateDetails(material.getDetails(), materialID);

		if (!detailsValid.equals(MessageValidationResult.IS_VALID))
		{
			log.error("details did not validate");
			return detailsValid;
		}

		MessageValidationResult ingestRecordValid = validateIngestRecord(material.getIngestRecords(),materialID);
		if (!ingestRecordValid.equals(MessageValidationResult.IS_VALID))
		{
			log.error("ingest record did not validate");
			return ingestRecordValid;
		}
		
		MessageValidationResult segmentDataValid = validateSegmentInfo(material.getIngestRecords(), materialID);

		if (!segmentDataValid.equals(MessageValidationResult.IS_VALID))
		{
			log.error("segmentation data did not validate");
			return segmentDataValid;
		}
		
		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateSegmentInfo(IngestRecords ingestRecords, String materialID)
	{
		// TODO validate segment info
		log.warn("no attempt made to validate segmentation information");
		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateIngestRecord(IngestRecords ingestRecords, String materialID)
	{
		// TODO validate ingest record
		log.warn("no attempt made to validate ingest record");
		return MessageValidationResult.IS_VALID;
	}

	private MessageValidationResult validateDetails(DetailType details, String materialID)
	{
		// not really much to validate for this types, its just strings
		return MessageValidationResult.IS_VALID;
	}

	@Override
	protected void typeCheck(Object unmarshalled) throws ClassCastException
	{ // NOSONAR
		// throwing unchecked exception as hint to users of class that this
		// method is likely to throw ClassCastException

		if (!(unmarshalled instanceof RuzzIngestRecord))
		{
			throw new ClassCastException(String.format(
					"unmarshalled type %s is not a RuzzIngestRecord",
					unmarshalled.getClass().toString()));
		}

	}

}
