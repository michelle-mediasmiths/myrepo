package com.mediasmiths.foxtel.mpa.processing;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilesPendingProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;

public abstract class MediaPickupProcessor<T> extends MessageProcessor<T>
{

	public MediaPickupProcessor(
			FilesPendingProcessingQueue filePathsPendingProcessing,
			MessageValidator<T> messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarhsaller,
			Marshaller marshaller,
			EventService eventService)
	{
		super(filePathsPendingProcessing, messageValidator, receiptWriter, unmarhsaller, marshaller, eventService);
	}

}
