package com.mediasmiths.foxtel.mpa.processing;

import static com.mediasmiths.foxtel.agent.Config.ARCHIVE_PATH;
import static com.mediasmiths.foxtel.agent.Config.FAILURE_PATH;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_FAILURE_ALERT_RECIPIENT;

import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.mpa.queue.PendingImportQueue;
import com.mediasmiths.foxtel.mpa.validation.MaterialExchangeValidator;
import com.mediasmiths.foxtel.mpa.validation.MediaCheck;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;

public class SingleMessageProcessor extends MaterialExchangeProcessor {

	@Inject
	public SingleMessageProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			PendingImportQueue filesPendingImport,
			MaterialExchangeValidator messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			MayamClient mayamClient,
			MatchMaker matchMaker,
			MediaCheck mediaCheck,
			@Named(FAILURE_PATH) String failurePath,
			@Named(ARCHIVE_PATH) String archivePath,
			AlertInterface alert,
			@Named(DELIVERY_FAILURE_ALERT_RECIPIENT) String deliveryFailureAlertReceipient) {
		super(filePathsPendingProcessing, filesPendingImport, messageValidator,
				receiptWriter, unmarhsaller, mayamClient, matchMaker,
				mediaCheck, failurePath, archivePath, alert,
				deliveryFailureAlertReceipient);
	}

	protected static Logger logger = Logger
			.getLogger(SingleMessageProcessor.class);

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
