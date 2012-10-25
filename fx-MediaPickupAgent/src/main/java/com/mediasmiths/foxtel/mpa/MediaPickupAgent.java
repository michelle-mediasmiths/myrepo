package com.mediasmiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public class MediaPickupAgent extends XmlWatchingAgent<Material> {

	private final Thread importerThread;
	private final Thread unmatchedThread;	
	
	@Inject
	public MediaPickupAgent(ConfigValidator configValidator,DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<Material> messageProcessor,
			ShutdownManager shutdownManager, Importer importer, UnmatchedMaterialProcessor unmatchedMaterialProcessor) throws JAXBException {
		super(configValidator,directoryWatcher, messageProcessor, shutdownManager);
		
		importerThread = new Thread(importer);
		importerThread.setName("Importer");
		registerThread(importerThread);
		
		unmatchedThread = new Thread(unmatchedMaterialProcessor);
		unmatchedThread.setName("Unmatched Material");
		registerThread(unmatchedThread);
		
	}
	
	@Override
	public void shutdown() {
		super.shutdown();
		importerThread.interrupt();
		unmatchedThread.interrupt();
	}

}
