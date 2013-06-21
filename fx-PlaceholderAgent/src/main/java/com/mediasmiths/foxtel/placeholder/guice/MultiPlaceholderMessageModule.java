package com.mediasmiths.foxtel.placeholder.guice;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.placeholder.processing.MultiPlaceholderMessageProcessor;
import com.mediasmiths.foxtel.placeholder.processing.PlaceholderMessageProcessor;

public class MultiPlaceholderMessageModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(PLACEHOLDERPROCESSOR_LITERAL).to(MultiPlaceholderMessageProcessor.class);
	}
	protected static final TypeLiteral<MessageProcessor<PlaceholderMessage>> PLACEHOLDERPROCESSOR_LITERAL =  new TypeLiteral<MessageProcessor<PlaceholderMessage>>(){};
}