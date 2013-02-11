package com.mediasmiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.foxtel.mpa.processing.MaterialExchangeProcessor;
import com.mediasmiths.foxtel.mpa.processing.RuzzPickupProcessor;
import com.mediasmiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public class MediaPickupAgent extends XmlWatchingAgent<Material> {

	@Inject
	public MediaPickupAgent(ConfigValidator configValidator,
			MaterialExchangeProcessor messageProcessor,RuzzPickupProcessor ruzzPickupProcessor,
			ShutdownManager shutdownManager, Importer importer, UnmatchedMaterialProcessor unmatchedMaterialProcessor) throws JAXBException {
		super(configValidator, messageProcessor, shutdownManager);
		
		this.addMessageProcessor(ruzzPickupProcessor);
		
		importer.startThread();
		registerService(importer);
		
		unmatchedMaterialProcessor.startThread();
		registerService(unmatchedMaterialProcessor);		
	}	
}
