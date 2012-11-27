package com.mediasmiths.mq.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.mayam.guice.MayamClientModule;

public class MqListenersModule extends AbstractModule {

	@Override
	protected void configure() {
//		install(new MayamClientModule());		
	}

}
