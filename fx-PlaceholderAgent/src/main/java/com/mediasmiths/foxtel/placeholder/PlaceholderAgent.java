package com.mediasmiths.foxtel.placeholder;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.XmlWatchingAgent;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;

import javax.xml.bind.JAXBException;

public class PlaceholderAgent extends XmlWatchingAgent<PlaceholderMessage>
{

	@Inject
	public PlaceholderAgent(
			ConfigValidator configValidator,
			MessageProcessor<PlaceholderMessage> messageProcessor,
			ShutdownManager shutdownManager) throws JAXBException
	{
		super(configValidator, messageProcessor, shutdownManager);
	}
}
