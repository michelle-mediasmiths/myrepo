package com.medismiths.foxtel.mpa.processing;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.mayam.MayamClient;
import com.medismiths.foxtel.mpa.validation.MediaExchangeValidator;

public class MaterialExchangeProcessor extends MessageProcessor<Material> {

	private final MayamClient mayamClient;
	
	public MaterialExchangeProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MediaExchangeValidator messageValidator, ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller, MayamClient mayamClient,
			@Named("agent.path.failure") String failurePath,
			@Named("agent.path.archive") String archivePath) {
		super(filePathsPendingProcessing,messageValidator,receiptWriter,unmarhsaller,failurePath,archivePath);
		this.mayamClient = mayamClient;
		logger.debug("Using failure path " + failurePath);
		logger.debug("Using archivePath path " + archivePath);
	}

	private static Logger logger = Logger.getLogger(MaterialExchangeProcessor.class);
	
	@Override
	protected String getIDFromMessage(Material message) {
		//TODO implement
		logger.fatal("getIDFromMessage not implemented");
		return "AAAAA";
	}

	@Override
	protected void processMessage(Material message)
			throws MessageProcessingFailedException {
		//TODO implement
		logger.fatal("processMessage not implemented");
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
