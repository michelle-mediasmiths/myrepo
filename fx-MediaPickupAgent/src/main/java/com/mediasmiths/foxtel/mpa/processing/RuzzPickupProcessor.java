package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.ARDOME_EMERGENCY_IMPORT_FOLDER;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailureReason;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord.Material.IngestRecords;
import com.mediasmiths.foxtel.generated.ruzz.SegmentationType;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.queue.RuzzFilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.foxtel.mpa.validation.RuzzValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class RuzzPickupProcessor extends MediaPickupProcessor<RuzzIngestRecord>
{

	protected final static Logger logger = Logger.getLogger(RuzzPickupProcessor.class);
	
	@Inject
	public RuzzPickupProcessor(
			RuzzFilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			RuzzValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			@Named(ARDOME_EMERGENCY_IMPORT_FOLDER) String emergencyImportFolder,
			EventService eventService)
	{
		super(filePathsPendingProcessing,filesPendingImport,messageValidator,receiptWriter,unmarhsaller,marshaller,mayamClient,matchMaker,mediaCheck,emergencyImportFolder,eventService);
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
	protected String updateMamWithMaterialInformation(RuzzIngestRecord message) throws MessageProcessingFailedException
	{
		
		Material material = message.getMaterial();
		String materialID = material.getMaterialID();
		logger.info(String.format("Received ruzz ingest message for material %s",materialID));
		
		DetailType details = material.getDetails();
		if(details == null){
			logger.warn(String.format("Received null details for material %s", materialID));
		}
		else{
			try
			{
				updateDetails(details,materialID);
			}
			catch (MayamClientException e)
			{
				logger.error("Error updating detail infromation for materialID "+materialID,e);
				throw new MessageProcessingFailedException(MessageProcessingFailureReason.MAYAM_CLIENT_EXCEPTION,e);
			}
		}
		
		IngestRecords ingestRecords = material.getIngestRecords();
		if(ingestRecords==null){
			logger.warn(String.format("Null ingest records for material %s", materialID));
		}
		else{
			updateIngestRecord(ingestRecords,materialID);
		}
		
		SegmentationType segments = material.getSegments();
		if(segments==null){
			logger.warn(String.format("Null segmentation information for material %s", materialID));
		}
		else{
			updateSegmentationInfo(segments,materialID);
		}
		
		return materialID;
	}

	private void updateSegmentationInfo(SegmentationType segments, String materialID)
	{
		logger.debug(String.format("updateSegmentationInfo for material %s",materialID));
		//TODO update segmentation info
		logger.warn("no attempt made to save segmentation information");
	}

	private void updateIngestRecord(IngestRecords ingestRecords, String materialID)
	{
		logger.debug(String.format("updateIngestRecord for material %s",materialID));
		//TODO : update channel conditions \ ingest records
		logger.warn("no attempt made to save channel conditions");
	}

	private void updateDetails(DetailType details,String materialID) throws MayamClientException
	{
		logger.debug(String.format("updateDetails for material %s",materialID));
		mayamClient.updateMaterial(details,materialID);
	}

}
