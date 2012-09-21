package com.mediasmiths.foxtel.placeholder.validation.channels;

import com.google.inject.AbstractModule;

public class ChannelValidatorModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ChannelValidator.class).to(ChannelValidatorImpl.class);
	}

}
