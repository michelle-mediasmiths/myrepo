package com.mediasmiths.foxtel.placeholder;

import javax.xml.bind.JAXBException;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.DirectoryWatchingQueuer;
import com.mediasmiths.foxtel.agent.MessageProcessor;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

public class PlaceholderAgent extends XmlWatchingAgent<PlaceholderMessage> {

	@Inject
	public PlaceholderAgent(DirectoryWatchingQueuer directoryWatcher,
			MessageProcessor<PlaceholderMessage> messageProcessor,
			ShutdownManager shutdownManager) throws JAXBException {
		super(directoryWatcher, messageProcessor, shutdownManager);
	}
}
