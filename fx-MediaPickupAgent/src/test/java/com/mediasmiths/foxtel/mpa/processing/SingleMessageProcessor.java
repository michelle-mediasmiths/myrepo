package com.mediasmiths.foxtel.mpa.processing;


import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.mpa.queue.MaterialExchangeFilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.mayam.MayamClient;

public class SingleMessageProcessor extends MaterialExchangeProcessor {

	@Inject
	public SingleMessageProcessor(
			MaterialExchangeFilesPendingProcessingQueue filePathsPendingProcessing,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			MayamClient mayamClient,
			EventService eventService, UnmatchedMaterialProcessor unmatchedProcessor){
		super(filePathsPendingProcessing, messageValidator,
				receiptWriter, unmarhsaller,marshaller, mayamClient, eventService,unmatchedProcessor);
	}

	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

	@Override
	public void run() {

		logger.trace("SingleMessageProcessor.run() enter");

		validateThenProcessPickupPackage(getFilePickup().take());

		logger.trace("SingleMessageProcessor.run() exit");

	}

}
