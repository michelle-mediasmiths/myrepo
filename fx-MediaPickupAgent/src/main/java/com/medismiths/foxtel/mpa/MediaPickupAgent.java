package com.medismiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.medismiths.foxtel.mpa.delivery.Importer;

public class MediaPickupAgent extends XmlWatchingAgent<Material> {

	private static Logger logger = Logger.getLogger(MediaPickupAgent.class);

	private final Importer importer;
	private final Thread importerThread;
	
	@Inject
	public MediaPickupAgent(ConfigValidator configValidator,DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<Material> messageProcessor,
			ShutdownManager shutdownManager, Importer importer) throws JAXBException {
		super(configValidator,directoryWatcher, messageProcessor, shutdownManager);
		
		this.importer=importer;
		this.importerThread = new Thread(importer);
		this.importerThread.setName("Importer");
		registerThread(importerThread);
	}

}
