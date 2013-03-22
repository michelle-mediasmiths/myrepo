package com.mediasmiths.foxtel.mpa.processing;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material.IngestRecords;
import com.mediasmiths.foxtel.generated.ruzz.SegmentationType;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.queue.RuzzFilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.foxtel.mpa.validation.RuzzValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.SegmentUtil;

public class RuzzPickupProcessor extends MediaPickupProcessor<RuzzIngestRecord>
{

	protected final static Logger logger = Logger.getLogger(RuzzPickupProcessor.class);

	@Inject
	public RuzzPickupProcessor(
			RuzzFilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			RuzzValidator messageValidator,
			ReceiptWriter receiptWriter,
			@Named("ruzz.unmarshaller") Unmarshaller unmarhsaller,
			@Named("ruzz.marshaller") Marshaller marshaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			EventService eventService)
	{
		super(
				filePathsPendingProcessing,
				filesPendingImport,
				messageValidator,
				receiptWriter,
				unmarhsaller,
				marshaller,
				mayamClient,
				matchMaker,
				mediaCheck,
				eventService);
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

	@Override
	protected MamUpdateResult updateMamWithMaterialInformation(RuzzIngestRecord message) throws MessageProcessingFailedException
	{

		Material material = message.getMaterial();
		String materialID = material.getMaterialID();
		logger.info(String.format("Received ruzz ingest message for material %s", materialID));

		DetailType details = material.getDetails();
		if (details == null)
		{
			logger.warn(String.format("Received null details for material %s", materialID));
		}
		else
		{
			try
			{
				updateDetails(details, materialID);
			}
			catch (MayamClientException e)
			{
				logger.error("Error updating detail infromation for materialID " + materialID, e);
				throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
			}
		}

		IngestRecords ingestRecords = material.getIngestRecords();
		if (ingestRecords == null)
		{
			logger.warn(String.format("Null ingest records for material %s", materialID));
		}
		else
		{
			updateIngestRecord(ingestRecords, materialID);
		}

		SegmentationType segments = material.getSegments();
		if (segments == null)
		{
			logger.warn(String.format("Null segmentation information for material %s", materialID));
		}
		else
		{
			try
			{
				updateSegmentationInfo(segments, materialID);
			}
			catch (MayamClientException e)
			{
				logger.error("Error setting natural breaks information on materialID " + materialID, e);
				throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION, e);
			}
		}

		return new MamUpdateResult(materialID, true);
	}

	private void updateSegmentationInfo(SegmentationType segments, String materialID) throws MayamClientException
	{
		logger.debug(String.format("updateSegmentationInfo for material %s", materialID));
		String naturalBreaks = SegmentUtil.ruzzSegmentTypeToHumanString(segments);
		mayamClient.setNaturalBreaks(materialID, naturalBreaks);
	}

	private void updateIngestRecord(IngestRecords ingestRecords, String materialID)
	{
		logger.debug(String.format("updateIngestRecord for material %s", materialID));
		// TODO : update channel conditions \ ingest records
		logger.warn("no attempt made to save channel conditions");
	}

	private void updateDetails(DetailType details, String materialID) throws MayamClientException
	{
		logger.debug(String.format("updateDetails for material %s", materialID));
		mayamClient.updateMaterial(details, materialID);
	}

}
