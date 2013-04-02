package com.mediasmiths.foxtel.mpa.processing;


import java.io.File;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.queue.MaterialExchangeFilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.mayam.MayamClient;

public class SingleMessageProcessor extends MaterialExchangeProcessor {

	@Inject
	public SingleMessageProcessor(
			MaterialExchangeFilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			EventService eventService){
		super(filePathsPendingProcessing, filesPendingImport, messageValidator,
				receiptWriter, unmarhsaller,marshaller, mayamClient, matchMaker,
				 eventService);
	}

	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

	@Override
	public void run() {

		logger.trace("SingleMessageProcessor.run() enter");

		File file = getFilePathsPending().take();
		String filePath = file.getAbsolutePath();
		validateThenProcessFile(filePath);

		logger.trace("SingleMessageProcessor.run() exit");

	}

}
