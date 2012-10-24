package com.mediasmiths.mq.guice;

import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_AUTH_TOKEN;
import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_ENDPOINT;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.MqListeners;

public class MqListenersModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MqListenersModule.class);

	@Override
	protected void configure() {
		install(new MayamClientModule());		
	}

}
