package com.mediasmiths.foxtel.mpa.processing;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.mayam.MayamClient;
import com.medismiths.foxtel.mpa.processing.MatchMaker;
import com.medismiths.foxtel.mpa.processing.MaterialExchangeProcessor;
import com.medismiths.foxtel.mpa.queue.PendingImportQueue;
import com.medismiths.foxtel.mpa.validation.MaterialExchangeValidator;

public class SingleMessageProcessor extends MaterialExchangeProcessor {


	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);
	
	@Inject
	public SingleMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter, Unmarshaller unmarhsaller,
			MayamClient mayamClient, MatchMaker matchMaker, String failurePath,
			String archivePath) {
		super(filePathsPendingProcessing, filesPendingImport, messageValidator,
				receiptWriter, unmarhsaller, mayamClient, matchMaker, failurePath,
				archivePath);		
	}
	
	@Override
	public void run() {

		logger.trace("SingleMessageProcessor.run() enter");

		try {
			String filePath = getFilePathsPending().take();
			validateThenProcessFile(filePath);
		} catch (InterruptedException e) {
			logger.info("Interruped!", e);
			stop();
		}

		logger.trace("SingleMessageProcessor.run() exit");

	}

}
