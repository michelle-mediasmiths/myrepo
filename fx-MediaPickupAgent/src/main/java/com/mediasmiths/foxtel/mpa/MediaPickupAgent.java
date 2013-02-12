package com.mediasmiths.foxtel.mpa;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

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

	private final static Logger log = Logger.getLogger(MediaPickupAgent.class);
	
	@Inject
	public MediaPickupAgent(ConfigValidator configValidator,
			MaterialExchangeProcessor messageProcessor,RuzzPickupProcessor ruzzPickupProcessor,
			ShutdownManager shutdownManager, Importer importer, UnmatchedMaterialProcessor unmatchedMaterialProcessor) throws JAXBException {
		super(configValidator, messageProcessor, shutdownManager);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Media pickup agent starting\n");
		sb.append("Material exchange directories : ");
		for(File f : messageProcessor.getFilePathsPending().getWatchedDirectories()){
			sb.append(f.getAbsolutePath() + "\n");
		}
		sb.append("Ruzz directories : ");
		for(File f : ruzzPickupProcessor.getFilePathsPending().getWatchedDirectories()){
			sb.append(f.getAbsolutePath() + "\n");
		}
		log.info(sb.toString());
		
		this.addMessageProcessor(ruzzPickupProcessor);
		
		importer.startThread();
		registerService(importer);
		
		unmatchedMaterialProcessor.startThread();
		registerService(unmatchedMaterialProcessor);		
	}	
}
