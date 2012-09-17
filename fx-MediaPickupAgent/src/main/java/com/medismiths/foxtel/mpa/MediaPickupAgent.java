package com.medismiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.medismiths.foxtel.mpa.delivery.Importer;
import com.medismiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;

public class MediaPickupAgent extends XmlWatchingAgent<Material> {

	private final Importer importer;
	private final UnmatchedMaterialProcessor unmatchedMaterialProcessor;	
	
	@Inject
	public MediaPickupAgent(ConfigValidator configValidator,DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<Material> messageProcessor,
			ShutdownManager shutdownManager, Importer importer, UnmatchedMaterialProcessor unmatchedMaterialProcessor) throws JAXBException {
		super(configValidator,directoryWatcher, messageProcessor, shutdownManager);
		
		this.importer=importer;
		Thread importerThread = new Thread(importer);
		importerThread.setName("Importer");
		registerThread(importerThread);
		
		this.unmatchedMaterialProcessor=unmatchedMaterialProcessor;
		Thread unmatchedThread = new Thread(unmatchedMaterialProcessor);
		unmatchedThread.setName("Unmatched Material");
		registerThread(unmatchedThread);
		
	}
	
	@Override
	public void shutdown() {
		super.shutdown();
		importer.stop();
		unmatchedMaterialProcessor.stop();
	}

}
