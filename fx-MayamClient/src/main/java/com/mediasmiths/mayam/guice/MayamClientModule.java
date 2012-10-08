package com.mediasmiths.mayam.guice;

import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_AUTH_TOKEN;
import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_ENDPOINT;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.AlertImpl;
import com.mediasmiths.mayam.AlertInterface;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;


public class MayamClientModule extends AbstractModule
{

	public final static String SETUP_TASKS_CLIENT = "SETUP_TASKS_CIENT";
	
	@Override
	protected void configure()
	{
		install(new AttributesModule());
		install(new MqModule("fxMayamClient"));
		bind(MayamClient.class).to(MayamClientImpl.class);
		
		bind(AlertInterface.class).to(AlertImpl.class);
//		bind(AttributeMapMapper.class).to(JacksonAttributeMapMapperImpl.class);
		
	}

	@Provides
	@Named(SETUP_TASKS_CLIENT)
	public TasksClient getSetupTasksClient(TasksClient tc,@Named(MAYAM_ENDPOINT) String endpoint,
			@Named(MAYAM_AUTH_TOKEN) String token) throws MalformedURLException{
		URL url = new URL(endpoint);
		return tc.setup(url, token);
	}
	
}
